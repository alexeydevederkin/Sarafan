import Vue from 'vue'

const profile = Vue.resource('/profile{/id}')

export default {
    all: () => profile.get(),
    get: id => profile.get({id}),
    changeSubscription: channelId => Vue.http.post(`/profile/change-subscription/${channelId}`),
    subscriberList: channelId => Vue.http.get(`/profile/get-subscribers/${channelId}`),
    subscriptionsList: () => Vue.http.get(`/profile/get-subscriptions`),
    changeSubscriptionStatus: subscriberId => Vue.http.post(`/profile/change-status/${subscriberId}`)
}
