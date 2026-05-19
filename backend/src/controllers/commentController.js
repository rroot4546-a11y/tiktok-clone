const Comment = require('../models/Comment');
const Video = require('../models/Video');
const Notification = require('../models/Notification');

exports.addComment = async (req, res, next) => {
  try {
    const { text, parentComment } = req.body;
    const videoId = req.params.videoId;

    const video = await Video.findById(videoId);
    if (!video) {
      return res.status(404).json({ error: 'Video not found' });
    }

    if (!video.allowComments) {
      return res.status(403).json({ error: 'Comments are disabled for this video' });
    }

    const comment = new Comment({
      user: req.userId,
      video: videoId,
      text,
      parentComment: parentComment || null,
    });

    await comment.save();

    if (parentComment) {
      await Comment.findByIdAndUpdate(parentComment, { $inc: { repliesCount: 1 } });
    }

    await Video.findByIdAndUpdate(videoId, { $inc: { commentsCount: 1 } });

    if (video.user.toString() !== req.userId.toString()) {
      await Notification.create({
        recipient: video.user,
        sender: req.userId,
        type: 'comment',
        video: videoId,
        comment: comment._id,
        message: text.substring(0, 100),
      });
    }

    const populatedComment = await Comment.findById(comment._id)
      .populate('user', 'username displayName avatar isVerified');

    res.status(201).json({ comment: populatedComment });
  } catch (error) {
    next(error);
  }
};

exports.getComments = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;
    const sort = req.query.sort === 'likes' ? { likesCount: -1 } : { createdAt: -1 };

    const comments = await Comment.find({
      video: req.params.videoId,
      parentComment: null,
    })
      .populate('user', 'username displayName avatar isVerified')
      .sort(sort)
      .skip(skip)
      .limit(limit);

    const total = await Comment.countDocuments({
      video: req.params.videoId,
      parentComment: null,
    });

    const enrichedComments = comments.map(c => {
      const obj = c.toObject();
      obj.isLiked = req.userId ? c.likes.includes(req.userId) : false;
      return obj;
    });

    res.json({
      comments: enrichedComments,
      total,
      page,
      hasMore: skip + comments.length < total,
    });
  } catch (error) {
    next(error);
  }
};

exports.getReplies = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const replies = await Comment.find({
      parentComment: req.params.commentId,
    })
      .populate('user', 'username displayName avatar isVerified')
      .sort({ createdAt: 1 })
      .skip(skip)
      .limit(limit);

    res.json({ replies });
  } catch (error) {
    next(error);
  }
};

exports.likeComment = async (req, res, next) => {
  try {
    const comment = await Comment.findById(req.params.commentId);
    if (!comment) {
      return res.status(404).json({ error: 'Comment not found' });
    }

    const isLiked = comment.likes.includes(req.userId);

    if (isLiked) {
      comment.likes.pull(req.userId);
      comment.likesCount = Math.max(0, comment.likesCount - 1);
    } else {
      comment.likes.push(req.userId);
      comment.likesCount += 1;
    }

    await comment.save();
    res.json({ isLiked: !isLiked, likesCount: comment.likesCount });
  } catch (error) {
    next(error);
  }
};

exports.deleteComment = async (req, res, next) => {
  try {
    const comment = await Comment.findById(req.params.commentId);
    if (!comment) {
      return res.status(404).json({ error: 'Comment not found' });
    }

    if (comment.user.toString() !== req.userId.toString() && req.user.role !== 'admin') {
      return res.status(403).json({ error: 'Not authorized' });
    }

    await Video.findByIdAndUpdate(comment.video, { $inc: { commentsCount: -1 } });

    if (comment.parentComment) {
      await Comment.findByIdAndUpdate(comment.parentComment, { $inc: { repliesCount: -1 } });
    }

    await Comment.deleteMany({ parentComment: comment._id });
    await comment.deleteOne();

    res.json({ message: 'Comment deleted' });
  } catch (error) {
    next(error);
  }
};

exports.pinComment = async (req, res, next) => {
  try {
    const comment = await Comment.findById(req.params.commentId).populate('video');

    if (!comment) {
      return res.status(404).json({ error: 'Comment not found' });
    }

    if (comment.video.user.toString() !== req.userId.toString()) {
      return res.status(403).json({ error: 'Only video owner can pin comments' });
    }

    await Comment.updateMany(
      { video: comment.video._id, isPinned: true },
      { isPinned: false }
    );

    comment.isPinned = !comment.isPinned;
    await comment.save();

    res.json({ isPinned: comment.isPinned });
  } catch (error) {
    next(error);
  }
};
