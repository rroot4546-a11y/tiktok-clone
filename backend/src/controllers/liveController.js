const LiveStream = require('../models/LiveStream');
const { v4: uuidv4 } = require('uuid');

exports.createLiveStream = async (req, res, next) => {
  try {
    const { title, hashtags } = req.body;

    const existingLive = await LiveStream.findOne({
      host: req.userId,
      status: 'live',
    });

    if (existingLive) {
      return res.status(400).json({ error: 'You already have an active live stream' });
    }

    const stream = new LiveStream({
      host: req.userId,
      title,
      streamKey: uuidv4(),
      hashtags: hashtags || [],
      status: 'live',
      startedAt: Date.now(),
    });

    await stream.save();

    const populated = await LiveStream.findById(stream._id)
      .populate('host', 'username displayName avatar isVerified followersCount');

    res.status(201).json({ stream: populated });
  } catch (error) {
    next(error);
  }
};

exports.endLiveStream = async (req, res, next) => {
  try {
    const stream = await LiveStream.findOne({
      _id: req.params.id,
      host: req.userId,
      status: 'live',
    });

    if (!stream) {
      return res.status(404).json({ error: 'Live stream not found' });
    }

    stream.status = 'ended';
    stream.endedAt = Date.now();
    stream.duration = Math.floor((Date.now() - stream.startedAt) / 1000);
    await stream.save();

    res.json({ stream });
  } catch (error) {
    next(error);
  }
};

exports.getLiveStreams = async (req, res, next) => {
  try {
    const streams = await LiveStream.find({ status: 'live' })
      .populate('host', 'username displayName avatar isVerified followersCount')
      .sort({ viewersCount: -1 });

    res.json({ streams });
  } catch (error) {
    next(error);
  }
};

exports.getLiveStream = async (req, res, next) => {
  try {
    const stream = await LiveStream.findById(req.params.id)
      .populate('host', 'username displayName avatar isVerified followersCount');

    if (!stream) {
      return res.status(404).json({ error: 'Live stream not found' });
    }

    res.json({ stream });
  } catch (error) {
    next(error);
  }
};

exports.sendGift = async (req, res, next) => {
  try {
    const { giftType, giftValue } = req.body;
    const stream = await LiveStream.findById(req.params.id);

    if (!stream || stream.status !== 'live') {
      return res.status(404).json({ error: 'Live stream not found or ended' });
    }

    stream.gifts.push({
      sender: req.userId,
      giftType,
      giftValue,
    });
    stream.totalGiftValue += giftValue;
    await stream.save();

    const io = req.app.get('io');
    if (io) {
      io.to(`live_${stream._id}`).emit('gift_received', {
        sender: req.user.username,
        giftType,
        giftValue,
      });
    }

    res.json({ message: 'Gift sent', totalGiftValue: stream.totalGiftValue });
  } catch (error) {
    next(error);
  }
};

exports.addComment = async (req, res, next) => {
  try {
    const { text } = req.body;
    const stream = await LiveStream.findById(req.params.id);

    if (!stream || stream.status !== 'live') {
      return res.status(404).json({ error: 'Live stream not found or ended' });
    }

    const comment = {
      user: req.userId,
      text,
      sentAt: Date.now(),
    };

    stream.comments.push(comment);
    await stream.save();

    const io = req.app.get('io');
    if (io) {
      io.to(`live_${stream._id}`).emit('live_comment', {
        user: { username: req.user.username, avatar: req.user.avatar },
        text,
      });
    }

    res.json({ message: 'Comment added' });
  } catch (error) {
    next(error);
  }
};
