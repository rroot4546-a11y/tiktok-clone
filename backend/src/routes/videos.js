const express = require('express');
const router = express.Router();
const videoController = require('../controllers/videoController');
const { authenticate, optionalAuth } = require('../middleware/auth');
const { uploadVideo } = require('../middleware/upload');

router.post('/upload', authenticate, uploadVideo.single('video'), videoController.uploadVideo);
router.get('/feed', optionalAuth, videoController.getFeed);
router.get('/following', authenticate, videoController.getFollowingFeed);
router.get('/trending', videoController.getTrending);
router.get('/hashtag/:tag', videoController.getVideosByHashtag);
router.get('/user/:userId', videoController.getUserVideos);
router.get('/:id', optionalAuth, videoController.getVideo);
router.post('/:id/like', authenticate, videoController.likeVideo);
router.post('/:id/save', authenticate, videoController.saveVideo);
router.post('/:id/share', authenticate, videoController.shareVideo);
router.post('/:id/report', authenticate, videoController.reportVideo);
router.delete('/:id', authenticate, videoController.deleteVideo);

module.exports = router;
