importScripts('https://www.gstatic.com/firebasejs/9.6.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.6.1/firebase-messaging-compat.js');

// TODO: Replace with your Firebase project configuration
const firebaseConfig = {
    apiKey: "AIzaSyAaqBkYM4a69GlaVda6b2c651wfJTyeRLk",
    authDomain: "taste-web-push.firebaseapp.com",
    projectId: "taste-web-push",
    storageBucket: "taste-web-push.firebasestorage.app",
    messagingSenderId: "62821911150",
    appId: "1:62821911150:web:db6a0df9ca7d4e439ef47f",
    measurementId: "G-5GGMJ81LTC"
};

firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

messaging.onBackgroundMessage(function (payload) {
    console.log('[firebase-messaging-sw.js] Received background message ', payload);

    // 클라이언트로 메시지 전달
    self.clients.matchAll({includeUncontrolled: true, type: 'window'}).then(clients => {
        clients.forEach(client => {
            client.postMessage({
                type: 'FCM_MESSAGE',
                payload: payload
            });
        });
    });

    const notificationTitle = payload.notification.title || '새 알림';
    const notificationBody = payload.notification.body || '내용 없음';
    const notificationImage = payload.notification.image || null;
    const notificationIcon = payload.notification.icon || '/firebase-image.png'; // Use a default icon if not provided

    const notificationOptions = {
        body: notificationBody,
        icon: notificationIcon, // Use the icon
        data: payload.data // Pass data to notificationclick event
    };

    if (notificationImage) {
        notificationOptions.image = notificationImage;
    }

    // Display the OS notification
    return self.registration.showNotification(notificationTitle, notificationOptions);
});


self.addEventListener('notificationclick', function (event) {
    event.notification.close();
    const clickedNotificationData = event.notification.data;
    if (clickedNotificationData && clickedNotificationData.redirectUrl) {
        clients.openWindow(clickedNotificationData.redirectUrl);
    } else if (clickedNotificationData && clickedNotificationData.contentId) {
        // Handle deep linking based on contentId if needed
        clients.openWindow('/'); // Or a default page
    } else {
        clients.openWindow('/');
    }
});
