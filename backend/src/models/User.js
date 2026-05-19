const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema({
  username: {
    type: String,
    required: true,
    unique: true,
    trim: true,
    minlength: 3,
    maxlength: 30,
  },
  email: {
    type: String,
    required: true,
    unique: true,
    trim: true,
    lowercase: true,
  },
  password: {
    type: String,
    minlength: 6,
    select: false,
  },
  displayName: {
    type: String,
    trim: true,
    maxlength: 50,
  },
  bio: {
    type: String,
    maxlength: 150,
    default: '',
  },
  avatar: {
    type: String,
    default: '',
  },
  coverImage: {
    type: String,
    default: '',
  },
  phoneNumber: {
    type: String,
    default: '',
  },
  dateOfBirth: Date,
  gender: {
    type: String,
    enum: ['male', 'female', 'other', 'prefer_not_to_say'],
  },
  followers: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
  following: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
  followersCount: { type: Number, default: 0 },
  followingCount: { type: Number, default: 0 },
  likesCount: { type: Number, default: 0 },
  videosCount: { type: Number, default: 0 },
  isPrivate: { type: Boolean, default: false },
  isVerified: { type: Boolean, default: false },
  isBanned: { type: Boolean, default: false },
  role: {
    type: String,
    enum: ['user', 'creator', 'moderator', 'admin'],
    default: 'user',
  },
  authProvider: {
    type: String,
    enum: ['local', 'google', 'facebook', 'phone'],
    default: 'local',
  },
  firebaseUid: String,
  googleId: String,
  facebookId: String,
  refreshToken: String,
  fcmToken: String,
  lastActive: { type: Date, default: Date.now },
  isOnline: { type: Boolean, default: false },
  blockedUsers: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
  savedVideos: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Video',
  }],
  interests: [String],
  language: { type: String, default: 'en' },
  coins: { type: Number, default: 0 },
  isPremium: { type: Boolean, default: false },
  premiumExpiry: Date,
  totalWatchTime: { type: Number, default: 0 },
}, {
  timestamps: true,
});

userSchema.index({ username: 'text', displayName: 'text' });

userSchema.pre('save', async function (next) {
  if (!this.isModified('password') || !this.password) return next();
  this.password = await bcrypt.hash(this.password, 12);
  next();
});

userSchema.methods.comparePassword = async function (candidatePassword) {
  return bcrypt.compare(candidatePassword, this.password);
};

userSchema.methods.toPublicProfile = function () {
  return {
    _id: this._id,
    username: this.username,
    displayName: this.displayName,
    bio: this.bio,
    avatar: this.avatar,
    coverImage: this.coverImage,
    followersCount: this.followersCount,
    followingCount: this.followingCount,
    likesCount: this.likesCount,
    videosCount: this.videosCount,
    isVerified: this.isVerified,
    isPrivate: this.isPrivate,
    isOnline: this.isOnline,
  };
};

module.exports = mongoose.model('User', userSchema);
