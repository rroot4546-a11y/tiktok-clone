const express = require('express');
const router = express.Router();
const chatController = require('../controllers/chatController');
const { authenticate } = require('../middleware/auth');

router.get('/conversations', authenticate, chatController.getConversations);
router.post('/conversations', authenticate, chatController.getOrCreateConversation);
router.post('/send', authenticate, chatController.sendMessage);
router.get('/:conversationId/messages', authenticate, chatController.getMessages);
router.put('/:conversationId/read', authenticate, chatController.markAsRead);
router.delete('/messages/:messageId', authenticate, chatController.deleteMessage);

module.exports = router;
