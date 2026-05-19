const express = require('express');
const router = express.Router();
const liveController = require('../controllers/liveController');
const { authenticate } = require('../middleware/auth');

router.get('/', liveController.getLiveStreams);
router.get('/:id', liveController.getLiveStream);
router.post('/create', authenticate, liveController.createLiveStream);
router.post('/:id/end', authenticate, liveController.endLiveStream);
router.post('/:id/gift', authenticate, liveController.sendGift);
router.post('/:id/comment', authenticate, liveController.addComment);

module.exports = router;
