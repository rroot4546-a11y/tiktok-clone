const User = require('../models/User');
const Video = require('../models/Video');
const Notification = require('../models/Notification');
const { sanitizeUser } = require('../utils/helpers');

exports.getProfile = async (req, res, next) => {
  try {
    const user = await User.findById(req.params.id)
      .select('-refreshToken -blockedUsers -savedVideos');

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    const profile = user.toPublicProfile();

    if (req.userId) {
      profile.isFollowing = user.followers.includes(req.userId);
      profile.isBlocked = false;
      const currentUser = await User.findById(req.userId);
      profile.isBlocked = currentUser?.blockedUsers?.includes(user._id) || false;
    }

    res.json({ user: profile });
  } catch (error) {
    next(error);
  }
};

exports.getProfileByUsername = async (req, res, next) => {
  try {
    const user = await User.findOne({ username: req.params.username })
      .select('-refreshToken -blockedUsers -savedVideos');

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    const profile = user.toPublicProfile();

    if (req.userId) {
      profile.isFollowing = user.followers.includes(req.userId);
    }

    res.json({ user: profile });
  } catch (error) {
    next(error);
  }
};

exports.updateProfile = async (req, res, next) => {
  try {
    const allowedFields = ['displayName', 'bio', 'gender', 'dateOfBirth', 'isPrivate', 'language'];
    const updates = {};

    for (const field of allowedFields) {
      if (req.body[field] !== undefined) {
        updates[field] = req.body[field];
      }
    }

    if (req.body.username) {
      const existing = await User.findOne({ username: req.body.username, _id: { $ne: req.userId } });
      if (existing) {
        return res.status(409).json({ error: 'Username already taken' });
      }
      updates.username = req.body.username;
    }

    const user = await User.findByIdAndUpdate(req.userId, updates, { new: true, runValidators: true });

    res.json({ user: sanitizeUser(user) });
  } catch (error) {
    next(error);
  }
};

exports.updateAvatar = async (req, res, next) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: 'Image file required' });
    }

    let avatarUrl = '';
    if (process.env.AWS_S3_BUCKET) {
      const { uploadToS3 } = require('../config/s3');
      avatarUrl = await uploadToS3(req.file, 'avatars');
    } else {
      avatarUrl = `data:${req.file.mimetype};base64,${req.file.buffer.toString('base64')}`;
    }

    const user = await User.findByIdAndUpdate(req.userId, { avatar: avatarUrl }, { new: true });

    res.json({ avatar: user.avatar });
  } catch (error) {
    next(error);
  }
};

exports.followUser = async (req, res, next) => {
  try {
    const targetUserId = req.params.id;

    if (targetUserId === req.userId.toString()) {
      return res.status(400).json({ error: 'Cannot follow yourself' });
    }

    const targetUser = await User.findById(targetUserId);
    if (!targetUser) {
      return res.status(404).json({ error: 'User not found' });
    }

    const isFollowing = targetUser.followers.includes(req.userId);

    if (isFollowing) {
      targetUser.followers.pull(req.userId);
      targetUser.followersCount = Math.max(0, targetUser.followersCount - 1);

      await User.findByIdAndUpdate(req.userId, {
        $pull: { following: targetUserId },
        $inc: { followingCount: -1 },
      });
    } else {
      targetUser.followers.push(req.userId);
      targetUser.followersCount += 1;

      await User.findByIdAndUpdate(req.userId, {
        $push: { following: targetUserId },
        $inc: { followingCount: 1 },
      });

      await Notification.create({
        recipient: targetUserId,
        sender: req.userId,
        type: 'follow',
      });
    }

    await targetUser.save();

    res.json({
      isFollowing: !isFollowing,
      followersCount: targetUser.followersCount,
    });
  } catch (error) {
    next(error);
  }
};

exports.getFollowers = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;

    const user = await User.findById(req.params.id)
      .populate({
        path: 'followers',
        select: 'username displayName avatar isVerified followersCount',
        options: { skip, limit },
      });

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    res.json({ followers: user.followers });
  } catch (error) {
    next(error);
  }
};

exports.getFollowing = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;

    const user = await User.findById(req.params.id)
      .populate({
        path: 'following',
        select: 'username displayName avatar isVerified followersCount',
        options: { skip, limit },
      });

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    res.json({ following: user.following });
  } catch (error) {
    next(error);
  }
};

exports.blockUser = async (req, res, next) => {
  try {
    const targetUserId = req.params.id;

    if (targetUserId === req.userId.toString()) {
      return res.status(400).json({ error: 'Cannot block yourself' });
    }

    const user = await User.findById(req.userId);
    const isBlocked = user.blockedUsers.includes(targetUserId);

    if (isBlocked) {
      user.blockedUsers.pull(targetUserId);
    } else {
      user.blockedUsers.push(targetUserId);
      user.following.pull(targetUserId);
      await User.findByIdAndUpdate(targetUserId, {
        $pull: { followers: req.userId },
        $inc: { followersCount: -1 },
      });
    }

    await user.save();
    res.json({ isBlocked: !isBlocked });
  } catch (error) {
    next(error);
  }
};

exports.searchUsers = async (req, res, next) => {
  try {
    const { q } = req.query;
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;

    if (!q || q.length < 1) {
      return res.status(400).json({ error: 'Search query required' });
    }

    const users = await User.find({
      $or: [
        { username: { $regex: q, $options: 'i' } },
        { displayName: { $regex: q, $options: 'i' } },
      ],
      isBanned: false,
    })
      .select('username displayName avatar isVerified followersCount')
      .skip(skip)
      .limit(limit);

    res.json({ users });
  } catch (error) {
    next(error);
  }
};

exports.getSuggestedUsers = async (req, res, next) => {
  try {
    const limit = parseInt(req.query.limit) || 10;
    const currentUser = req.userId ? await User.findById(req.userId) : null;
    const excludeIds = currentUser ? [req.userId, ...currentUser.following] : [];

    const users = await User.find({
      _id: { $nin: excludeIds },
      isBanned: false,
    })
      .select('username displayName avatar isVerified followersCount bio')
      .sort({ followersCount: -1 })
      .limit(limit);

    res.json({ users });
  } catch (error) {
    next(error);
  }
};

exports.getSavedVideos = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;

    const user = await User.findById(req.userId)
      .populate({
        path: 'savedVideos',
        populate: { path: 'user', select: 'username displayName avatar isVerified' },
        options: { skip, limit, sort: { createdAt: -1 } },
      });

    res.json({ videos: user.savedVideos });
  } catch (error) {
    next(error);
  }
};

exports.getLikedVideos = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;

    const videos = await Video.find({ likes: req.params.id, status: 'active' })
      .populate('user', 'username displayName avatar isVerified')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    res.json({ videos });
  } catch (error) {
    next(error);
  }
};
