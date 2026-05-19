const Notification = require('../models/Notification');

exports.getNotifications = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;

    const notifications = await Notification.find({ recipient: req.userId })
      .populate('sender', 'username displayName avatar isVerified')
      .populate('video', 'thumbnailUrl caption')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    const unreadCount = await Notification.countDocuments({
      recipient: req.userId,
      isRead: false,
    });

    res.json({ notifications, unreadCount });
  } catch (error) {
    next(error);
  }
};

exports.markAsRead = async (req, res, next) => {
  try {
    const { notificationId } = req.params;

    if (notificationId) {
      await Notification.findByIdAndUpdate(notificationId, {
        isRead: true,
        readAt: Date.now(),
      });
    }

    res.json({ message: 'Notification marked as read' });
  } catch (error) {
    next(error);
  }
};

exports.markAllAsRead = async (req, res, next) => {
  try {
    await Notification.updateMany(
      { recipient: req.userId, isRead: false },
      { isRead: true, readAt: Date.now() }
    );

    res.json({ message: 'All notifications marked as read' });
  } catch (error) {
    next(error);
  }
};

exports.deleteNotification = async (req, res, next) => {
  try {
    await Notification.findOneAndDelete({
      _id: req.params.notificationId,
      recipient: req.userId,
    });

    res.json({ message: 'Notification deleted' });
  } catch (error) {
    next(error);
  }
};

exports.getUnreadCount = async (req, res, next) => {
  try {
    const count = await Notification.countDocuments({
      recipient: req.userId,
      isRead: false,
    });

    res.json({ unreadCount: count });
  } catch (error) {
    next(error);
  }
};
