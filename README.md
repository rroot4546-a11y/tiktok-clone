# TikTok Clone

A full-stack TikTok clone with an Android app (Kotlin/Jetpack Compose) and Node.js backend.

## Architecture

```
tiktok-clone/
├── android/          # Android app (Kotlin + Jetpack Compose)
│   └── app/src/main/java/com/tiktokclone/
│       ├── data/         # API, Models, Repositories, Local storage
│       ├── di/           # Hilt dependency injection
│       ├── navigation/   # Compose navigation
│       ├── ui/           # Screens & ViewModels
│       └── utils/        # Extensions & utilities
├── backend/          # Node.js + Express API
│   └── src/
│       ├── config/       # DB, Firebase, S3 config
│       ├── controllers/  # Route handlers
│       ├── middleware/    # Auth, upload, error handling
│       ├── models/       # Mongoose schemas
│       ├── routes/       # Express routes
│       ├── services/     # Business logic
│       ├── sockets/      # Socket.IO handlers
│       └── utils/        # Helpers
```

## Tech Stack

### Android App
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Repository Pattern
- **DI:** Hilt (Dagger)
- **Networking:** Retrofit + OkHttp
- **Image Loading:** Coil
- **Video:** ExoPlayer (Media3)
- **Auth:** Firebase Authentication
- **Real-time:** Socket.IO Client
- **Camera:** CameraX
- **Storage:** DataStore Preferences
- **Navigation:** Compose Navigation

### Backend
- **Runtime:** Node.js + Express
- **Database:** MongoDB + Mongoose
- **Auth:** JWT + Firebase Admin
- **Real-time:** Socket.IO
- **Storage:** AWS S3
- **Video Processing:** FFmpeg
- **Security:** Helmet, Rate Limiting, CORS

## Features

### Authentication
- Email/password registration & login
- Google Sign-In
- Facebook Login
- Phone number OTP
- JWT token auth with refresh tokens
- Biometric login support
- Forgot/reset password

### Home Feed
- Infinite vertical scrolling (TikTok-style)
- For You + Following tabs
- Auto-play videos
- Double-tap to like animation
- Preloading next videos
- Smart recommendation algorithm

### Video
- Upload from gallery
- Record with in-app camera
- Trim, filters, effects
- Add music/captions/hashtags
- Background upload
- Speed control
- Duet & Stitch support

### Social
- Like, comment, share, save videos
- Follow/unfollow users
- Block/report users
- Comment replies & likes
- Pin comments

### Messaging
- Private 1-on-1 chat
- Image/video/voice messages
- Read receipts & typing indicator
- Online status
- Push notifications

### Live Streaming
- Go live
- Real-time comments
- Virtual gifts
- Viewer count
- Live moderation

### Search & Discovery
- Search users, videos, hashtags
- Trending hashtags
- Suggested users
- Voice search

### Profile
- Editable profile (avatar, bio, username)
- Video grid
- Liked videos tab
- Followers/following lists
- Private account option

### Notifications
- Like/comment/follow notifications
- Push notifications (FCM)
- Unread count badge

### Admin Panel
- User management (ban/unban, roles)
- Content moderation
- Reports management
- Analytics dashboard

## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/google` | Google auth |
| POST | `/api/auth/facebook` | Facebook auth |
| POST | `/api/auth/phone` | Phone OTP auth |
| POST | `/api/auth/refresh-token` | Refresh JWT |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Get current user |

### Videos
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/videos/upload` | Upload video |
| GET | `/api/videos/feed` | Get For You feed |
| GET | `/api/videos/following` | Get following feed |
| GET | `/api/videos/trending` | Get trending videos |
| POST | `/api/videos/:id/like` | Like/unlike video |
| POST | `/api/videos/:id/save` | Save/unsave video |
| DELETE | `/api/videos/:id` | Delete video |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/:id` | Get user profile |
| PUT | `/api/users/profile` | Update profile |
| POST | `/api/users/:id/follow` | Follow/unfollow |
| POST | `/api/users/:id/block` | Block/unblock |
| GET | `/api/users/search` | Search users |

### Chat
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/chat/conversations` | Get conversations |
| POST | `/api/chat/send` | Send message |
| GET | `/api/chat/:id/messages` | Get messages |

### More endpoints: `/api/comments`, `/api/notifications`, `/api/search`, `/api/admin`, `/api/live`

## Setup

### Backend
```bash
cd backend
cp .env.example .env
# Edit .env with your MongoDB URI, JWT secrets, AWS keys, Firebase config
npm install
npm run dev
```

### Android
1. Open `android/` in Android Studio
2. Add `google-services.json` from Firebase Console
3. Update `BASE_URL` in `app/build.gradle.kts` for your server
4. Build and run

## Database Collections
- `users` - User accounts and profiles
- `videos` - Video content and metadata
- `comments` - Video comments and replies
- `messages` / `conversations` - Chat system
- `notifications` - User notifications
- `hashtags` - Hashtag tracking
- `reports` - Content reports
- `livestreams` - Live streaming sessions

## Security
- Encrypted API calls (HTTPS)
- JWT token authentication
- Rate limiting
- Input validation
- Helmet security headers
- Anti-spam protection
- Content moderation system

## License
MIT
