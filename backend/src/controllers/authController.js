const User = require('../models/User');
const jwt = require('jsonwebtoken');
const { generateTokens, sanitizeUser } = require('../utils/helpers');
const { verifyFirebaseToken } = require('../config/firebase');

exports.register = async (req, res, next) => {
  try {
    const { username, email, password, displayName } = req.body;

    const existingUser = await User.findOne({ $or: [{ email }, { username }] });
    if (existingUser) {
      return res.status(409).json({
        error: existingUser.email === email ? 'Email already registered' : 'Username already taken',
      });
    }

    const user = new User({
      username,
      email,
      password,
      displayName: displayName || username,
    });
    await user.save();

    const tokens = generateTokens(user._id);
    user.refreshToken = tokens.refreshToken;
    await user.save();

    res.status(201).json({
      user: sanitizeUser(user),
      ...tokens,
    });
  } catch (error) {
    next(error);
  }
};

exports.login = async (req, res, next) => {
  try {
    const { email, password } = req.body;

    const user = await User.findOne({ email }).select('+password');
    if (!user || !user.password) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const isMatch = await user.comparePassword(password);
    if (!isMatch) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const tokens = generateTokens(user._id);
    user.refreshToken = tokens.refreshToken;
    user.isOnline = true;
    user.lastActive = Date.now();
    await user.save();

    res.json({
      user: sanitizeUser(user),
      ...tokens,
    });
  } catch (error) {
    next(error);
  }
};

exports.googleAuth = async (req, res, next) => {
  try {
    const { idToken } = req.body;
    const decoded = await verifyFirebaseToken(idToken);

    let user = await User.findOne({ $or: [{ googleId: decoded.uid }, { email: decoded.email }] });

    if (!user) {
      user = new User({
        username: decoded.email.split('@')[0] + '_' + Date.now().toString(36),
        email: decoded.email,
        displayName: decoded.name || decoded.email.split('@')[0],
        avatar: decoded.picture || '',
        googleId: decoded.uid,
        firebaseUid: decoded.uid,
        authProvider: 'google',
      });
    } else {
      user.googleId = decoded.uid;
      user.firebaseUid = decoded.uid;
    }

    const tokens = generateTokens(user._id);
    user.refreshToken = tokens.refreshToken;
    user.isOnline = true;
    await user.save();

    res.json({ user: sanitizeUser(user), ...tokens });
  } catch (error) {
    next(error);
  }
};

exports.facebookAuth = async (req, res, next) => {
  try {
    const { idToken } = req.body;
    const decoded = await verifyFirebaseToken(idToken);

    let user = await User.findOne({ $or: [{ facebookId: decoded.uid }, { email: decoded.email }] });

    if (!user) {
      user = new User({
        username: (decoded.name || 'user').replace(/\s/g, '') + '_' + Date.now().toString(36),
        email: decoded.email || `${decoded.uid}@facebook.com`,
        displayName: decoded.name || 'User',
        avatar: decoded.picture || '',
        facebookId: decoded.uid,
        firebaseUid: decoded.uid,
        authProvider: 'facebook',
      });
    } else {
      user.facebookId = decoded.uid;
      user.firebaseUid = decoded.uid;
    }

    const tokens = generateTokens(user._id);
    user.refreshToken = tokens.refreshToken;
    user.isOnline = true;
    await user.save();

    res.json({ user: sanitizeUser(user), ...tokens });
  } catch (error) {
    next(error);
  }
};

exports.phoneAuth = async (req, res, next) => {
  try {
    const { idToken } = req.body;
    const decoded = await verifyFirebaseToken(idToken);

    let user = await User.findOne({ $or: [{ firebaseUid: decoded.uid }, { phoneNumber: decoded.phone_number }] });

    if (!user) {
      user = new User({
        username: 'user_' + Date.now().toString(36),
        email: `${decoded.uid}@phone.tiktok-clone.com`,
        phoneNumber: decoded.phone_number,
        firebaseUid: decoded.uid,
        authProvider: 'phone',
      });
    }

    const tokens = generateTokens(user._id);
    user.refreshToken = tokens.refreshToken;
    user.isOnline = true;
    await user.save();

    res.json({ user: sanitizeUser(user), ...tokens });
  } catch (error) {
    next(error);
  }
};

exports.refreshToken = async (req, res, next) => {
  try {
    const { refreshToken } = req.body;
    if (!refreshToken) {
      return res.status(400).json({ error: 'Refresh token required' });
    }

    const decoded = jwt.verify(refreshToken, process.env.JWT_REFRESH_SECRET);
    const user = await User.findById(decoded.userId);

    if (!user || user.refreshToken !== refreshToken) {
      return res.status(401).json({ error: 'Invalid refresh token' });
    }

    const tokens = generateTokens(user._id);
    user.refreshToken = tokens.refreshToken;
    await user.save();

    res.json(tokens);
  } catch (error) {
    return res.status(401).json({ error: 'Invalid refresh token' });
  }
};

exports.logout = async (req, res, next) => {
  try {
    req.user.refreshToken = null;
    req.user.isOnline = false;
    req.user.lastActive = Date.now();
    await req.user.save();

    res.json({ message: 'Logged out successfully' });
  } catch (error) {
    next(error);
  }
};

exports.forgotPassword = async (req, res, next) => {
  try {
    const { email } = req.body;
    const user = await User.findOne({ email });
    if (!user) {
      return res.json({ message: 'If the email exists, a reset link has been sent' });
    }

    const resetToken = jwt.sign({ userId: user._id }, process.env.JWT_SECRET, { expiresIn: '1h' });

    res.json({
      message: 'Password reset token generated',
      resetToken,
    });
  } catch (error) {
    next(error);
  }
};

exports.resetPassword = async (req, res, next) => {
  try {
    const { token, newPassword } = req.body;
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    const user = await User.findById(decoded.userId);

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    user.password = newPassword;
    await user.save();

    res.json({ message: 'Password reset successfully' });
  } catch (error) {
    return res.status(400).json({ error: 'Invalid or expired reset token' });
  }
};

exports.getMe = async (req, res) => {
  res.json({ user: sanitizeUser(req.user) });
};

exports.updateFCMToken = async (req, res, next) => {
  try {
    const { fcmToken } = req.body;
    req.user.fcmToken = fcmToken;
    await req.user.save();
    res.json({ message: 'FCM token updated' });
  } catch (error) {
    next(error);
  }
};
