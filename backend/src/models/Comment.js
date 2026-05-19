const mongoose = require('mongoose');

const commentSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  video: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Video',
    required: true,
    index: true,
  },
  text: {
    type: String,
    required: true,
    maxlength: 500,
  },
  parentComment: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Comment',
    default: null,
  },
  likes: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
  likesCount: { type: Number, default: 0 },
  repliesCount: { type: Number, default: 0 },
  mentions: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
  isPinned: { type: Boolean, default: false },
  isReported: { type: Boolean, default: false },
}, {
  timestamps: true,
});

commentSchema.index({ video: 1, createdAt: -1 });

module.exports = mongoose.model('Comment', commentSchema);
