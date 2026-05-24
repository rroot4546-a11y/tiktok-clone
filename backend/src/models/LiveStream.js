const mongoose = require('mongoose');

const liveStreamSchema = new mongoose.Schema({
  host: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  title: {
    type: String,
    required: true,
    maxlength: 100,
  },
  thumbnailUrl: String,
  streamKey: {
    type: String,
    unique: true,
  },
  status: {
    type: String,
    enum: ['scheduled', 'live', 'ended'],
    default: 'scheduled',
  },
  viewersCount: { type: Number, default: 0 },
  peakViewers: { type: Number, default: 0 },
  totalViewers: { type: Number, default: 0 },
  gifts: [{
    sender: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    giftType: String,
    giftValue: Number,
    sentAt: { type: Date, default: Date.now },
  }],
  totalGiftValue: { type: Number, default: 0 },
  comments: [{
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    text: String,
    sentAt: { type: Date, default: Date.now },
  }],
  blockedUsers: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
  moderators: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
  startedAt: Date,
  endedAt: Date,
  duration: Number,
  hashtags: [String],
}, {
  timestamps: true,
});

module.exports = mongoose.model('LiveStream', liveStreamSchema);
