const AWS = require('aws-sdk');

const s3 = new AWS.S3({
  accessKeyId: process.env.AWS_ACCESS_KEY_ID,
  secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY,
  region: process.env.AWS_REGION || 'us-east-1',
});

const uploadToS3 = async (file, folder = 'videos') => {
  const params = {
    Bucket: process.env.AWS_S3_BUCKET,
    Key: `${folder}/${Date.now()}-${file.originalname}`,
    Body: file.buffer || require('fs').createReadStream(file.path),
    ContentType: file.mimetype,
    ACL: 'public-read',
  };

  const result = await s3.upload(params).promise();
  return result.Location;
};

const deleteFromS3 = async (fileUrl) => {
  const key = fileUrl.split('.com/')[1];
  const params = {
    Bucket: process.env.AWS_S3_BUCKET,
    Key: key,
  };

  await s3.deleteObject(params).promise();
};

const getSignedUrl = (key, expires = 3600) => {
  return s3.getSignedUrl('getObject', {
    Bucket: process.env.AWS_S3_BUCKET,
    Key: key,
    Expires: expires,
  });
};

module.exports = { s3, uploadToS3, deleteFromS3, getSignedUrl };
