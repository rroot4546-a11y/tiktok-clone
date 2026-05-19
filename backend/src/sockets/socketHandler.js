const jwt = require('jsonwebtoken');
const User = require('../models/User');

const setupSocketIO = (io) => {
  io.use(async (socket, next) => {
    try {
      const token = socket.handshake.auth.token || socket.handshake.query.token;
      if (!token) {
        return next(new Error('Authentication required'));
      }

      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      const user = await User.findById(decoded.userId);
      if (!user) {
        return next(new Error('User not found'));
      }

      socket.userId = user._id.toString();
      socket.user = user;
      next();
    } catch (error) {
      next(new Error('Authentication failed'));
    }
  });

  io.on('connection', async (socket) => {
    console.log(`User connected: ${socket.userId}`);

    socket.join(`user_${socket.userId}`);

    await User.findByIdAndUpdate(socket.userId, {
      isOnline: true,
      lastActive: Date.now(),
    });

    socket.broadcast.emit('user_online', { userId: socket.userId });

    socket.on('join_live', (streamId) => {
      socket.join(`live_${streamId}`);
      io.to(`live_${streamId}`).emit('viewer_joined', {
        userId: socket.userId,
        username: socket.user.username,
      });
    });

    socket.on('leave_live', (streamId) => {
      socket.leave(`live_${streamId}`);
      io.to(`live_${streamId}`).emit('viewer_left', {
        userId: socket.userId,
      });
    });

    socket.on('typing', ({ conversationId }) => {
      socket.to(`conversation_${conversationId}`).emit('user_typing', {
        userId: socket.userId,
        username: socket.user.username,
      });
    });

    socket.on('stop_typing', ({ conversationId }) => {
      socket.to(`conversation_${conversationId}`).emit('user_stop_typing', {
        userId: socket.userId,
      });
    });

    socket.on('join_conversation', (conversationId) => {
      socket.join(`conversation_${conversationId}`);
    });

    socket.on('leave_conversation', (conversationId) => {
      socket.leave(`conversation_${conversationId}`);
    });

    socket.on('video_watched', async ({ videoId, watchTime }) => {
      try {
        const Video = require('../models/Video');
        await Video.findByIdAndUpdate(videoId, {
          $inc: { viewsCount: 1 },
          $set: {
            'engagement.avgWatchTime':
              watchTime,
          },
        });

        await User.findByIdAndUpdate(socket.userId, {
          $inc: { totalWatchTime: watchTime },
        });
      } catch (error) {
        console.error('Error tracking video watch:', error);
      }
    });

    socket.on('disconnect', async () => {
      console.log(`User disconnected: ${socket.userId}`);

      await User.findByIdAndUpdate(socket.userId, {
        isOnline: false,
        lastActive: Date.now(),
      });

      socket.broadcast.emit('user_offline', { userId: socket.userId });
    });
  });
};

module.exports = setupSocketIO;
