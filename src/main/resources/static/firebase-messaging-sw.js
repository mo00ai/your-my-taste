importScripts('https://www.gstatic.com/firebasejs/8.10.0/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/8.10.0/firebase-messaging.js');

const firebaseConfig = {
    apiKey: "AIzaSyAmnbh0Elmt4KV-p3AqVhGLVulRz39LoMo",
    authDomain: "webpush-56042.firebaseapp.com",
    projectId: "webpush-56042",
    storageBucket: "webpush-56042.firebasestorage.app",
    messagingSenderId: "955852041033",
    appId: "1:955852041033:web:3dc89ec02b35578c960898",
    measurementId: "G-F8CV4K3ES5"
};

// Initialize Firebase in the service worker
firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
    console.log('[sw.js] Received background message ', payload);
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        icon: '/firebase/firebase-logo.png'
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});