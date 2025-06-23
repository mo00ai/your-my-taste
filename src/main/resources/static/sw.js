self.addEventListener('notificationclick', function (event) {
    event.notification.close();
    event.waitUntil(clients.openWindow(event.notification.data.url));
});

self.addEventListener('push', function (event) {
    console.log('🔔 Received push event:', event);

    if (!event.data) {
        console.log('❌ No data in push event');
        return;
    }

    let data = {};
    try {
        data = event.data.json();
    } catch (e) {
        console.log('❌ Error parsing push data:', e);
        return;
    }

    console.log('✅ Parsed push data:', data);

    const title = data.title || '🔔 알림';
    const options = {
        body: data.content || '내용 없음',
        data: {
            url: data.redirectUrl || '/'
        }
    };

    event.waitUntil(self.registration.showNotification(title, options));
});
