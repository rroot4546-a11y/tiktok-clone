const Video = require('../models/Video');
const User = require('../models/User');
const Hashtag = require('../models/Hashtag');
const Notification = require('../models/Notification');
const { extractHashtags, calculateTrendingScore } = require('../utils/helpers');
const { uploadToS3 } = require('../config/s3');

exports.uploadVideo = async (req, res, next) => {
  try {
    const { caption, music, allowComments, allowDuet, allowStitch, allowDownload, isPrivate } = req.body;

    if (!req.file) {
      return res.status(400).json({ error: 'Video file is required' });
    }

    let videoUrl = `/uploads/${req.file.filename}`;

    if (process.env.AWS_S3_BUCKET) {
      try {
        videoUrl = await uploadToS3(req.file);
      } catch (s3Error) {
        console.warn('S3 upload failed, using local storage:', s3Error.message);
      }
    }

    const hashtags = extractHashtags(caption);

    const video = new Video({
      user: req.userId,
      videoUrl,
      caption,
      hashtags,
      music: music ? JSON.parse(music) : { name: 'Original Sound', artist: req.user.username },
      duration: req.body.duration || 0,
      allowComments: allowComments !== 'false',
      allowDuet: allowDuet !== 'false',
      allowStitch: allowStitch !== 'false',
      allowDownload: allowDownload !== 'false',
      isPrivate: isPrivate === 'true',
      fileSize: req.file.size,
    });

    await video.save();

    for (const tag of hashtags) {
      await Hashtag.findOneAndUpdate(
        { name: tag },
        { $inc: { videosCount: 1 } },
        { upsert: true }
      );
    }

    await User.findByIdAndUpdate(req.userId, { $inc: { videosCount: 1 } });

    const populatedVideo = await Video.findById(video._id).populate('user', 'username displayName avatar isVerified');

    res.status(201).json({ video: populatedVideo });
  } catch (error) {
    next(error);
  }
};

exports.getFeed = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const query = { status: 'active', isPrivate: false };

    if (req.userId) {
      const user = await User.findById(req.userId);
      if (user?.blockedUsers?.length > 0) {
        query.user = { $nin: user.blockedUsers };
      }
    }

    const videos = await Video.find(query)
      .populate('user', 'username displayName avatar isVerified followersCount')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    const enrichedVideos = videos.map(video => {
      const v = video.toObject();
      v.isLiked = req.userId ? video.likes.includes(req.userId) : false;
      v.isSaved = false;
      return v;
    });

    if (req.userId) {
      const user = await User.findById(req.userId);
      enrichedVideos.forEach(v => {
        v.isSaved = user?.savedVideos?.includes(v._id) || false;
      });
    }

    const total = await Video.countDocuments(query);

    res.json({
      videos: enrichedVideos,
      page,
      totalPages: Math.ceil(total / limit),
      hasMore: skip + videos.length < total,
    });
  } catch (error) {
    next(error);
  }
};

exports.getFollowingFeed = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const user = await User.findById(req.userId);
    const following = user.following || [];

    const videos = await Video.find({
      user: { $in: following },
      status: 'active',
    })
      .populate('user', 'username displayName avatar isVerified')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    const enrichedVideos = videos.map(v => {
      const obj = v.toObject();
      obj.isLiked = v.likes.includes(req.userId);
      obj.isSaved = user.savedVideos?.includes(v._id) || false;
      return obj;
    });

    res.json({ videos: enrichedVideos, page });
  } catch (error) {
    next(error);
  }
};

exports.getVideo = async (req, res, next) => {
  try {
    const video = await Video.findById(req.params.id)
      .populate('user', 'username displayName avatar isVerified followersCount');

    if (!video || video.status === 'removed') {
      return res.status(404).json({ error: 'Video not found' });
    }

    video.viewsCount += 1;
    await video.save();

    const v = video.toObject();
    v.isLiked = req.userId ? video.likes.includes(req.userId) : false;

    res.json({ video: v });
  } catch (error) {
    next(error);
  }
};

exports.likeVideo = async (req, res, next) => {
  try {
    const video = await Video.findById(req.params.id);
    if (!video) {
      return res.status(404).json({ error: 'Video not found' });
    }

    const isLiked = video.likes.includes(req.userId);

    if (isLiked) {
      video.likes.pull(req.userId);
      video.likesCount = Math.max(0, video.likesCount - 1);
      await User.findByIdAndUpdate(video.user, { $inc: { likesCount: -1 } });
    } else {
      video.likes.push(req.userId);
      video.likesCount += 1;
      await User.findByIdAndUpdate(video.user, { $inc: { likesCount: 1 } });

      if (video.user.toString() !== req.userId.toString()) {
        await Notification.create({
          recipient: video.user,
          sender: req.userId,
          type: 'like',
          video: video._id,
        });
      }
    }

    await video.save();

    res.json({ isLiked: !isLiked, likesCount: video.likesCount });
  } catch (error) {
    next(error);
  }
};

exports.saveVideo = async (req, res, next) => {
  try {
    const user = await User.findById(req.userId);
    const videoId = req.params.id;
    const isSaved = user.savedVideos.includes(videoId);

    if (isSaved) {
      user.savedVideos.pull(videoId);
      await Video.findByIdAndUpdate(videoId, { $inc: { savesCount: -1 } });
    } else {
      user.savedVideos.push(videoId);
      await Video.findByIdAndUpdate(videoId, { $inc: { savesCount: 1 } });
    }

    await user.save();
    res.json({ isSaved: !isSaved });
  } catch (error) {
    next(error);
  }
};

exports.shareVideo = async (req, res, next) => {
  try {
    await Video.findByIdAndUpdate(req.params.id, { $inc: { sharesCount: 1 } });
    res.json({ message: 'Share count updated' });
  } catch (error) {
    next(error);
  }
};

exports.deleteVideo = async (req, res, next) => {
  try {
    const video = await Video.findById(req.params.id);
    if (!video) {
      return res.status(404).json({ error: 'Video not found' });
    }

    if (video.user.toString() !== req.userId.toString() && req.user.role !== 'admin') {
      return res.status(403).json({ error: 'Not authorized' });
    }

    video.status = 'removed';
    await video.save();
    await User.findByIdAndUpdate(video.user, { $inc: { videosCount: -1 } });

    res.json({ message: 'Video deleted' });
  } catch (error) {
    next(error);
  }
};

exports.getUserVideos = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;

    const videos = await Video.find({ user: req.params.userId, status: 'active' })
      .populate('user', 'username displayName avatar isVerified')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    res.json({ videos });
  } catch (error) {
    next(error);
  }
};

exports.getTrending = async (req, res, next) => {
  try {
    const limit = parseInt(req.query.limit) || 20;
    const timeframe = new Date(Date.now() - 24 * 60 * 60 * 1000);

    const videos = await Video.find({
      status: 'active',
      isPrivate: false,
      createdAt: { $gte: timeframe },
    })
      .populate('user', 'username displayName avatar isVerified')
      .sort({ viewsCount: -1, likesCount: -1 })
      .limit(limit);

    res.json({ videos });
  } catch (error) {
    next(error);
  }
};

exports.getVideosByHashtag = async (req, res, next) => {
  try {
    const { tag } = req.params;
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;

    const videos = await Video.find({
      hashtags: tag.toLowerCase(),
      status: 'active',
      isPrivate: false,
    })
      .populate('user', 'username displayName avatar isVerified')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    const hashtag = await Hashtag.findOne({ name: tag.toLowerCase() });

    res.json({ videos, hashtag });
  } catch (error) {
    next(error);
  }
};

exports.reportVideo = async (req, res, next) => {
  try {
    const Report = require('../models/Report');
    const { reason, description } = req.body;

    await Report.create({
      reporter: req.userId,
      targetType: 'video',
      targetId: req.params.id,
      reason,
      description,
    });

    await Video.findByIdAndUpdate(req.params.id, {
      isReported: true,
      $inc: { reportCount: 1 },
    });

    res.json({ message: 'Video reported' });
  } catch (error) {
    next(error);
  }
};
