const { Message, Conversation } = require('../models/Message');
const User = require('../models/User');

exports.getConversations = async (req, res, next) => {
  try {
    const conversations = await Conversation.find({
      participants: req.userId,
    })
      .populate('participants', 'username displayName avatar isVerified isOnline lastActive')
      .populate('lastMessage')
      .sort({ lastMessageAt: -1 });

    const enriched = conversations.map(conv => {
      const c = conv.toObject();
      c.unreadCount = conv.unreadCount?.get(req.userId.toString()) || 0;
      c.otherUser = c.participants.find(p => p._id.toString() !== req.userId.toString());
      return c;
    });

    res.json({ conversations: enriched });
  } catch (error) {
    next(error);
  }
};

exports.getOrCreateConversation = async (req, res, next) => {
  try {
    const { userId } = req.body;

    let conversation = await Conversation.findOne({
      participants: { $all: [req.userId, userId] },
      isGroup: false,
    }).populate('participants', 'username displayName avatar isVerified isOnline');

    if (!conversation) {
      conversation = new Conversation({
        participants: [req.userId, userId],
      });
      await conversation.save();
      await conversation.populate('participants', 'username displayName avatar isVerified isOnline');
    }

    res.json({ conversation });
  } catch (error) {
    next(error);
  }
};

exports.sendMessage = async (req, res, next) => {
  try {
    const { conversationId, content, type, mediaUrl, sharedVideo } = req.body;

    const conversation = await Conversation.findById(conversationId);
    if (!conversation || !conversation.participants.includes(req.userId)) {
      return res.status(403).json({ error: 'Not a participant' });
    }

    const message = new Message({
      conversation: conversationId,
      sender: req.userId,
      content,
      type: type || 'text',
      mediaUrl,
      sharedVideo,
      readBy: [{ user: req.userId }],
    });

    await message.save();

    conversation.lastMessage = message._id;
    conversation.lastMessageAt = Date.now();

    conversation.participants.forEach(participantId => {
      if (participantId.toString() !== req.userId.toString()) {
        const current = conversation.unreadCount?.get(participantId.toString()) || 0;
        conversation.unreadCount.set(participantId.toString(), current + 1);
      }
    });

    await conversation.save();

    const populatedMessage = await Message.findById(message._id)
      .populate('sender', 'username displayName avatar');

    const io = req.app.get('io');
    if (io) {
      conversation.participants.forEach(participantId => {
        if (participantId.toString() !== req.userId.toString()) {
          io.to(`user_${participantId}`).emit('new_message', {
            message: populatedMessage,
            conversationId,
          });
        }
      });
    }

    res.status(201).json({ message: populatedMessage });
  } catch (error) {
    next(error);
  }
};

exports.getMessages = async (req, res, next) => {
  try {
    const { conversationId } = req.params;
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 50;
    const skip = (page - 1) * limit;

    const conversation = await Conversation.findById(conversationId);
    if (!conversation || !conversation.participants.includes(req.userId)) {
      return res.status(403).json({ error: 'Not a participant' });
    }

    const messages = await Message.find({ conversation: conversationId, isDeleted: false })
      .populate('sender', 'username displayName avatar')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    conversation.unreadCount.set(req.userId.toString(), 0);
    await conversation.save();

    res.json({ messages: messages.reverse() });
  } catch (error) {
    next(error);
  }
};

exports.markAsRead = async (req, res, next) => {
  try {
    const { conversationId } = req.params;

    const conversation = await Conversation.findById(conversationId);
    if (!conversation) {
      return res.status(404).json({ error: 'Conversation not found' });
    }

    await Message.updateMany(
      {
        conversation: conversationId,
        'readBy.user': { $ne: req.userId },
      },
      {
        $push: { readBy: { user: req.userId, readAt: Date.now() } },
      }
    );

    conversation.unreadCount.set(req.userId.toString(), 0);
    await conversation.save();

    res.json({ message: 'Messages marked as read' });
  } catch (error) {
    next(error);
  }
};

exports.deleteMessage = async (req, res, next) => {
  try {
    const message = await Message.findById(req.params.messageId);
    if (!message) {
      return res.status(404).json({ error: 'Message not found' });
    }

    if (message.sender.toString() !== req.userId.toString()) {
      return res.status(403).json({ error: 'Not authorized' });
    }

    message.isDeleted = true;
    message.deletedAt = Date.now();
    message.content = '';
    await message.save();

    res.json({ message: 'Message deleted' });
  } catch (error) {
    next(error);
  }
};
