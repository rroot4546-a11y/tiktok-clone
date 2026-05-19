const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');
const { authenticate, optionalAuth } = require('../middleware/auth');
const { uploadImage } = require('../middleware/upload');

router.get('/search', userController.searchUsers);
router.get('/suggested', optionalAuth, userController.getSuggestedUsers);
router.get('/saved-videos', authenticate, userController.getSavedVideos);
router.get('/profile/:username', optionalAuth, userController.getProfileByUsername);
router.get('/:id', optionalAuth, userController.getProfile);
router.get('/:id/followers', userController.getFollowers);
router.get('/:id/following', userController.getFollowing);
router.get('/:id/liked-videos', userController.getLikedVideos);
router.put('/profile', authenticate, userController.updateProfile);
router.put('/avatar', authenticate, uploadImage.single('avatar'), userController.updateAvatar);
router.post('/:id/follow', authenticate, userController.followUser);
router.post('/:id/block', authenticate, userController.blockUser);

module.exports = router;
