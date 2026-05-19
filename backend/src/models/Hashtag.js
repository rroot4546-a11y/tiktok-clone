const mongoose = require('mongoose');

const hashtagSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
    unique: true,
    trim: true,
    lowercase: true,
  },
  videosCount: { type: Number, default: 0 },
  viewsCount: { type: Number, default: 0 },
  isBlocked: { type: Boolean, default: false },
  isTrending: { type: Boolean, default: false },
  trendingScore: { type: Number, default: 0 },
}, {
  timestamps: true,
});

hashtagSchema.index({ name: 'text' });
hashtagSchema.index({ trendingScore: -1 });

module.exports = mongoose.model('Hashtag', hashtagSchema);
