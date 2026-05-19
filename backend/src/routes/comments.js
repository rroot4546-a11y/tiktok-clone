const express = require('express');
const router = express.Router();
const commentController = require('../controllers/commentController');
const { authenticate, optionalAuth } = require('../middleware/auth');

router.post('/video/:videoId', authenticate, commentController.addComment);
router.get('/video/:videoId', optionalAuth, commentController.getComments);
router.get('/:commentId/replies', optionalAuth, commentController.getReplies);
router.post('/:commentId/like', authenticate, commentController.likeComment);
router.post('/:commentId/pin', authenticate, commentController.pinComment);
router.delete('/:commentId', authenticate, commentController.deleteComment);

module.exports = router;
