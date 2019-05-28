const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

//import admin module
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


exports.sendNewOrderNotification = functions.database.ref('orders_restaurateur/{uid}/{orderId}').onWrite((snapshot,context) => {

    if (snapshot.before.exists()) {
        console.log("This is not a new order, we should not notify this event");
        return null;
    }

    if (!snapshot.after.exists()) {
        console.log("Order deleted, we should not notify it");
        return null;
    }

    const uuid = context.params.uid;
    console.log('sendNewOrderNotification called')
    console.log('User to send notification', uuid);

    var ref = admin.database().ref(`users/${uuid}/token`);
    return ref.once("value", function(snapshot){
        const payload = {
            notification: {
                title: 'New order',
                body: 'A user just completed a new order, please confirm or decline it'
            }
        };

        admin.messaging().sendToDevice(snapshot.val(), payload)

    }, function (errorObject) {
        console.log("The read failed: " + errorObject.code);
    });
})

exports.sendOrderAssignedToRiderNotification = functions.database.ref('orders_rider/{uid}/{orderId}/').onWrite((snapshot,context) => {

    if (snapshot.before.exists()) {
        console.log("This is not a new order, we should not notify this event");
        return null;
    }

    if (!snapshot.after.exists()) {
        console.log("Order deleted, we should not notify it");
        return null;
    }

    const uuid = context.params.uid;
    console.log('sendOrderAssignedToRiderNotification called');
    console.log('User to send notification', uuid);

    var ref = admin.database().ref(`users/${uuid}/token`);
    return ref.once("value", function(snapshot){
        const payload = {
            notification: {
                title: 'New order',
                body: 'An order has been assigned to you for delivery, please confirm or decline it'
            }
        };

        admin.messaging().sendToDevice(snapshot.val(), payload)

    }, function (errorObject) {
        console.log("The read failed: " + errorObject.code);
    });
})

exports.sendOrderStatusUpdated = functions.database.ref('orders_customer/{uid}/{orderId}/').onWrite((snapshot,context) => {
    console.log('sendOrderStatusUpdated called');

    if (!snapshot.before.exists()) {
        console.log("New order added, we should not notify it");
        return null;
    }

    if (!snapshot.after.exists()) {
        console.log("Order deleted, we should not notify it");
        return null;
    }

    const uuid = context.params.uid;
    console.log('User to send notification', uuid);

    var myTitle = "Order updated";
    var myBody = ""

    if (snapshot.before.child("order_status").val() === snapshot.after.child("order_status").val()) {
        //riderId has probably been updated
        myBody = "A rider has been assigned to your order";
    } else {
        myBody = "The status of one of your orders has been updated to " + snapshot.after.child("order_status").val();
    }


    var ref = admin.database().ref(`users/${uuid}/token`);
    return ref.once("value", function(snapshot){
        const payload = {
            notification: {
                title: myTitle,
                body: myBody
            }
        };

        admin.messaging().sendToDevice(snapshot.val(), payload)

    }, function (errorObject) {
        console.log("The read failed: " + errorObject.code);
    });
})

exports.updateRestaurantRating = functions.database.ref('restaurants_ratings/{restaurantuid}').onWrite((snapshot,context) => {
    console.log('updateRestaurantRating called');

    const restaurantid = context.params.restaurantuid;
    console.log('Restaurant with new review:', restaurantid);

    var dbRef = admin.database().ref('restaurants/' + restaurantid);

    if (!snapshot.after.exists()) {
        console.log("Error while trying to compute review stats");
        return null;
    }
    
    var count = 0;
    var total = 0;
    snapshot.after.forEach((child) => {
        count++;
        total = total + child.child("rate").val();
    })

    dbRef.child("review_count").set(count);
    dbRef.child("review_avg").set(total/count);

    console.log("Average rating: " + total/count);

    console.log('Updated statistics');
    return true;
})

exports.updateRiderRating = functions.database.ref('riders_ratings/{rideruid}').onWrite((snapshot,context) => {
    console.log('updateRiderRating called');

    const riderid = context.params.rideruid;
    console.log('Rider with new review:', riderid);

    var dbRef = admin.database().ref('riders/' + riderid);

    if (!snapshot.after.exists()) {
        console.log("Error while trying to compute review stats");
        return null;
    }
    
    var count = 0;
    var total = 0;
    snapshot.after.forEach((child) => {
        count++;
        total = total + child.child("rate").val();
    })

    dbRef.child("review_count").set(count);
    dbRef.child("review_avg").set(total/count);

    console.log("Average rating: " + total/count);

    console.log('Updated statistics');
    return true;
})

function updateDishesUsingMap(orderedDishesMap, restaurantid) {
    var query = admin.database().ref('dishes/' + restaurantid).orderByKey();

    query.once("value").then(function(snapshot) {
        snapshot.forEach(function(child) {
            var currentQuantity = child.child("dish_count").val();
            console.log("Considering dish: " + child.key + " current quantity: " + currentQuantity);
            var total = 0;
            if (typeof currentQuantity !== 'undefined') {
                total = total + currentQuantity;
            }
            var ordered = orderedDishesMap.get(child.key);
            if (typeof ordered !== 'undefined') {
                total = total + ordered;
            }
            console.log("Considering dish: " + child.key + " updating quantity: " + total);
            //child.ref("dish_count").set(total);
            admin.database().ref("dishes/" + restaurantid + "/" + child.key + "/dish_count").set(total);
        })
        console.log('Updated dishes count');
        return true;
    }).catch(error => {
        console.log("Error 2: ", error);
    });
    return true;
}

exports.updateDishStatistics = functions.database.ref('orders_restaurateur/{restaurantId}/{orderId}').onWrite((snapshot,context) => {
    console.log('updateDishStatistics called');

    if (snapshot.before.exists()) {
        console.log("This is not a new order");
        return null;
    }

    const resid = context.params.restaurantId;
    const orderid = context.params.orderId;
    console.log('Restaurant with new order:', resid, " orderid: ", orderid);

    var order = snapshot.after.val();
    console.log("Order:", order);

    var query1 = admin.database().ref('orders_restaurateur/' + resid + '/' + orderid + '/' + "dishes" +'/').orderByKey();

    query1.once("value").then((dishes) => {
        var orderedDishesMap = new Map();
        dishes.forEach((dish) => {
            var key = dish.key;
            var quantity = dish.val();
            orderedDishesMap.set(key, quantity)
            console.log("For dish: " + key + " ordered quantity: " + quantity);
        });

        updateDishesUsingMap(orderedDishesMap, resid);
        
        return true;
    }).catch((error) => {
        console.log("Error 1: ", error);
    });
    // query.once("value").then(function(snapshot) {
    //     snapshot.forEach(function(childSnapshot) {
    //         // key will be "ada" the first time and "alan" the second time
            
    //     });
    //     return true;
    // }).catch(error => {
    //     console.log("Error 1");
    // });
    
    return true;
})

