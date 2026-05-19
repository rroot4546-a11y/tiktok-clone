const jwt = require('jsonwebtoken');

const generateTokens = (userId) => {
  const accessToken = jwt.sign(
    { userId },
    process.env.JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRE || '7d' }
  );

  const refreshToken = jwt.sign(
    { userId },
    process.env.JWT_REFRESH_SECRET,
    { expiresIn: process.env.JWT_REFRESH_EXPIRE || '30d' }
  );

  return { accessToken, refreshToken };
};

const extractHashtags = (text) => {
  if (!text) return [];
  const regex = /#(\w+)/g;
  const matches = text.match(regex);
  return matches ? matches.map(tag => tag.slice(1).toLowerCase()) : [];
};

const extractMentions = (text) => {
  if (!text) return [];
  const regex = /@(\w+)/g;
  const matches = text.match(regex);
  return matches ? matches.map(mention => mention.slice(1)) : [];
};

const paginate = (query, page = 1, limit = 20) => {
  const skip = (page - 1) * limit;
  return query.skip(skip).limit(limit);
};

const calculateTrendingScore = (video) => {
  const ageInHours = (Date.now() - video.createdAt) / (1000 * 60 * 60);
  const engagement = (video.likesCount * 2) + (video.commentsCount * 3) +
    (video.sharesCount * 5) + (video.viewsCount * 0.1);
  return engagement / Math.pow(ageInHours + 2, 1.5);
};

const sanitizeUser = (user) => {
  const { password, refreshToken, __v, ...sanitized } = user.toObject ? user.toObject() : user;
  return sanitized;
};

module.exports = {
  generateTokens,
  extractHashtags,
  extractMentions,
  paginate,
  calculateTrendingScore,
  sanitizeUser,
};
