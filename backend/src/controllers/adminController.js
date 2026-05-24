const User = require('../models/User');
const Video = require('../models/Video');
const Comment = require('../models/Comment');
const Report = require('../models/Report');

exports.getDashboard = async (req, res, next) => {
  try {
    const [
      totalUsers,
      totalVideos,
      totalComments,
      pendingReports,
      bannedUsers,
      activeToday,
    ] = await Promise.all([
      User.countDocuments(),
      Video.countDocuments({ status: 'active' }),
      Comment.countDocuments(),
      Report.countDocuments({ status: 'pending' }),
      User.countDocuments({ isBanned: true }),
      User.countDocuments({
        lastActive: { $gte: new Date(Date.now() - 24 * 60 * 60 * 1000) },
      }),
    ]);

    const newUsersToday = await User.countDocuments({
      createdAt: { $gte: new Date(Date.now() - 24 * 60 * 60 * 1000) },
    });

    const newVideosToday = await Video.countDocuments({
      createdAt: { $gte: new Date(Date.now() - 24 * 60 * 60 * 1000) },
    });

    res.json({
      totalUsers,
      totalVideos,
      totalComments,
      pendingReports,
      bannedUsers,
      activeToday,
      newUsersToday,
      newVideosToday,
    });
  } catch (error) {
    next(error);
  }
};

exports.getUsers = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;
    const { search, role, banned } = req.query;

    const query = {};
    if (search) {
      query.$or = [
        { username: { $regex: search, $options: 'i' } },
        { email: { $regex: search, $options: 'i' } },
      ];
    }
    if (role) query.role = role;
    if (banned === 'true') query.isBanned = true;

    const [users, total] = await Promise.all([
      User.find(query).select('-password -refreshToken').sort({ createdAt: -1 }).skip(skip).limit(limit),
      User.countDocuments(query),
    ]);

    res.json({ users, total, page, totalPages: Math.ceil(total / limit) });
  } catch (error) {
    next(error);
  }
};

exports.banUser = async (req, res, next) => {
  try {
    const user = await User.findById(req.params.id);
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    user.isBanned = !user.isBanned;
    await user.save();

    if (user.isBanned) {
      await Video.updateMany({ user: user._id }, { status: 'removed' });
    }

    res.json({ isBanned: user.isBanned });
  } catch (error) {
    next(error);
  }
};

exports.deleteVideo = async (req, res, next) => {
  try {
    const video = await Video.findByIdAndUpdate(req.params.id, { status: 'removed' }, { new: true });
    if (!video) {
      return res.status(404).json({ error: 'Video not found' });
    }

    await User.findByIdAndUpdate(video.user, { $inc: { videosCount: -1 } });

    res.json({ message: 'Video removed' });
  } catch (error) {
    next(error);
  }
};

exports.getReports = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;
    const { status } = req.query;

    const query = status ? { status } : {};

    const [reports, total] = await Promise.all([
      Report.find(query)
        .populate('reporter', 'username displayName avatar')
        .populate('resolvedBy', 'username')
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limit),
      Report.countDocuments(query),
    ]);

    res.json({ reports, total, page, totalPages: Math.ceil(total / limit) });
  } catch (error) {
    next(error);
  }
};

exports.resolveReport = async (req, res, next) => {
  try {
    const { action } = req.body;

    const report = await Report.findByIdAndUpdate(
      req.params.id,
      {
        status: 'resolved',
        resolvedBy: req.userId,
        resolvedAt: Date.now(),
        action: action || 'none',
      },
      { new: true }
    );

    if (!report) {
      return res.status(404).json({ error: 'Report not found' });
    }

    if (action === 'content_removed' && report.targetType === 'video') {
      await Video.findByIdAndUpdate(report.targetId, { status: 'removed' });
    } else if (action === 'user_banned') {
      await User.findByIdAndUpdate(report.targetId, { isBanned: true });
    }

    res.json({ report });
  } catch (error) {
    next(error);
  }
};

exports.updateUserRole = async (req, res, next) => {
  try {
    const { role } = req.body;
    const user = await User.findByIdAndUpdate(
      req.params.id,
      { role },
      { new: true, runValidators: true }
    ).select('-password -refreshToken');

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    res.json({ user });
  } catch (error) {
    next(error);
  }
};

exports.getAnalytics = async (req, res, next) => {
  try {
    const days = parseInt(req.query.days) || 7;
    const startDate = new Date(Date.now() - days * 24 * 60 * 60 * 1000);

    const [userGrowth, videoGrowth] = await Promise.all([
      User.aggregate([
        { $match: { createdAt: { $gte: startDate } } },
        {
          $group: {
            _id: { $dateToString: { format: '%Y-%m-%d', date: '$createdAt' } },
            count: { $sum: 1 },
          },
        },
        { $sort: { _id: 1 } },
      ]),
      Video.aggregate([
        { $match: { createdAt: { $gte: startDate } } },
        {
          $group: {
            _id: { $dateToString: { format: '%Y-%m-%d', date: '$createdAt' } },
            count: { $sum: 1 },
          },
        },
        { $sort: { _id: 1 } },
      ]),
    ]);

    res.json({ userGrowth, videoGrowth });
  } catch (error) {
    next(error);
  }
};
