const express = require('express');
const router = express.Router();
const adminController = require('../controllers/adminController');
const { authenticate, requireAdmin } = require('../middleware/auth');

router.use(authenticate, requireAdmin);

router.get('/dashboard', adminController.getDashboard);
router.get('/users', adminController.getUsers);
router.get('/reports', adminController.getReports);
router.get('/analytics', adminController.getAnalytics);
router.put('/users/:id/ban', adminController.banUser);
router.put('/users/:id/role', adminController.updateUserRole);
router.delete('/videos/:id', adminController.deleteVideo);
router.put('/reports/:id/resolve', adminController.resolveReport);

module.exports = router;
