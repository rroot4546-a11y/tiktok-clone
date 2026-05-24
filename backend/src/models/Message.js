const mongoose = require('mongoose');

const messageSchema = new mongoose.Schema({
  conversation: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Conversation',
    required: true,
    index: true,
  },
  sender: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  type: {
    type: String,
    enum: ['text', 'image', 'video', 'voice', 'gif', 'sticker', 'video_share'],
    default: 'text',
  },
  content: {
    type: String,
    default: '',
  },
  mediaUrl: String,
  mediaThumbnail: String,
  mediaDuration: Number,
  sharedVideo: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Video',
  },
  readBy: [{
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
    },
    readAt: { type: Date, default: Date.now },
  }],
  isDeleted: { type: Boolean, default: false },
  deletedAt: Date,
}, {
  timestamps: true,
});

const conversationSchema = new mongoose.Schema({
  participants: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
  lastMessage: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Message',
  },
  lastMessageAt: { type: Date, default: Date.now },
  isGroup: { type: Boolean, default: false },
  groupName: String,
  groupAvatar: String,
  unreadCount: {
    type: Map,
    of: Number,
    default: {},
  },
}, {
  timestamps: true,
});

conversationSchema.index({ participants: 1 });
conversationSchema.index({ lastMessageAt: -1 });

const Message = mongoose.model('Message', messageSchema);
const Conversation = mongoose.model('Conversation', conversationSchema);

module.exports = { Message, Conversation };
