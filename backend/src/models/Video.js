const mongoose = require('mongoose');

const videoSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
    index: true,
  },
  videoUrl: {
    type: String,
    required: true,
  },
  thumbnailUrl: {
    type: String,
    default: '',
  },
  caption: {
    type: String,
    maxlength: 2200,
    default: '',
  },
  music: {
    name: { type: String, default: 'Original Sound' },
    artist: String,
    url: String,
    duration: Number,
  },
  hashtags: [{
    type: String,
    trim: true,
    lowercase: true,
  }],
  mentions: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
  likes: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
  likesCount: { type: Number, default: 0 },
  commentsCount: { type: Number, default: 0 },
  sharesCount: { type: Number, default: 0 },
  viewsCount: { type: Number, default: 0 },
  savesCount: { type: Number, default: 0 },
  duration: { type: Number, default: 0 },
  width: { type: Number, default: 1080 },
  height: { type: Number, default: 1920 },
  fileSize: { type: Number, default: 0 },
  format: { type: String, default: 'mp4' },
  isPrivate: { type: Boolean, default: false },
  allowComments: { type: Boolean, default: true },
  allowDuet: { type: Boolean, default: true },
  allowStitch: { type: Boolean, default: true },
  allowDownload: { type: Boolean, default: true },
  isDuet: { type: Boolean, default: false },
  duetWith: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Video',
  },
  isStitch: { type: Boolean, default: false },
  stitchWith: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Video',
  },
  status: {
    type: String,
    enum: ['processing', 'active', 'flagged', 'removed'],
    default: 'active',
  },
  filters: [String],
  effects: [String],
  speed: { type: Number, default: 1 },
  location: {
    name: String,
    lat: Number,
    lng: Number,
  },
  engagement: {
    avgWatchTime: { type: Number, default: 0 },
    completionRate: { type: Number, default: 0 },
    shareRate: { type: Number, default: 0 },
  },
  isAd: { type: Boolean, default: false },
  isReported: { type: Boolean, default: false },
  reportCount: { type: Number, default: 0 },
}, {
  timestamps: true,
});

videoSchema.index({ hashtags: 1 });
videoSchema.index({ createdAt: -1 });
videoSchema.index({ likesCount: -1 });
videoSchema.index({ viewsCount: -1 });
videoSchema.index({ caption: 'text' });

module.exports = mongoose.model('Video', videoSchema);
