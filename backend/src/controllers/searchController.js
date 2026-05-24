const User = require('../models/User');
const Video = require('../models/Video');
const Hashtag = require('../models/Hashtag');

exports.search = async (req, res, next) => {
  try {
    const { q, type } = req.query;
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;

    if (!q || q.length < 1) {
      return res.status(400).json({ error: 'Search query required' });
    }

    const results = {};

    if (!type || type === 'users') {
      results.users = await User.find({
        $or: [
          { username: { $regex: q, $options: 'i' } },
          { displayName: { $regex: q, $options: 'i' } },
        ],
        isBanned: false,
      })
        .select('username displayName avatar isVerified followersCount')
        .skip(skip)
        .limit(type ? limit : 5);
    }

    if (!type || type === 'videos') {
      results.videos = await Video.find({
        $or: [
          { caption: { $regex: q, $options: 'i' } },
          { hashtags: { $regex: q, $options: 'i' } },
        ],
        status: 'active',
        isPrivate: false,
      })
        .populate('user', 'username displayName avatar isVerified')
        .skip(skip)
        .limit(type ? limit : 5);
    }

    if (!type || type === 'hashtags') {
      results.hashtags = await Hashtag.find({
        name: { $regex: q, $options: 'i' },
        isBlocked: false,
      })
        .sort({ videosCount: -1 })
        .skip(skip)
        .limit(type ? limit : 5);
    }

    res.json(results);
  } catch (error) {
    next(error);
  }
};

exports.getTrending = async (req, res, next) => {
  try {
    const hashtags = await Hashtag.find({ isBlocked: false })
      .sort({ trendingScore: -1 })
      .limit(20);

    const users = await User.find({ isBanned: false })
      .select('username displayName avatar isVerified followersCount')
      .sort({ followersCount: -1 })
      .limit(10);

    res.json({ hashtags, users });
  } catch (error) {
    next(error);
  }
};

exports.getSuggestions = async (req, res, next) => {
  try {
    const { q } = req.query;

    if (!q || q.length < 1) {
      const trending = await Hashtag.find({ isBlocked: false })
        .sort({ trendingScore: -1 })
        .limit(10)
        .select('name videosCount');
      return res.json({ suggestions: trending.map(h => ({ type: 'hashtag', text: h.name, count: h.videosCount })) });
    }

    const [users, hashtags] = await Promise.all([
      User.find({ username: { $regex: q, $options: 'i' }, isBanned: false })
        .select('username displayName avatar')
        .limit(5),
      Hashtag.find({ name: { $regex: q, $options: 'i' }, isBlocked: false })
        .select('name videosCount')
        .limit(5),
    ]);

    const suggestions = [
      ...hashtags.map(h => ({ type: 'hashtag', text: h.name, count: h.videosCount })),
      ...users.map(u => ({ type: 'user', text: u.username, avatar: u.avatar, displayName: u.displayName })),
    ];

    res.json({ suggestions });
  } catch (error) {
    next(error);
  }
};
