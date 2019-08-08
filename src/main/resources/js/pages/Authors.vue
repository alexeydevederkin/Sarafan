<template>
    <v-container>
        <v-layout justify-space-around>
            <v-list>
                <v-list-tile
                        v-for="(author, index) in authors"
                        :key="author.id"
                >
                    <user-link
                            :user="author"
                            size="24"
                    ></user-link>

                    <v-btn v-if="$store.state.profile.id !== author.id"
                           @click="changeSubscription(index, author.id)"
                    >
                        {{author.subscribed ? "Unsubscribe" : "Subscribe"}}
                    </v-btn>
                </v-list-tile>
            </v-list>
        </v-layout>
    </v-container>
</template>

<script>
    import profileApi from '../api/profile'
    import UserLink from '../components/UserLink.vue'
    export default {
        name: 'Authors',
        components: {UserLink},
        data() {
            return {
                authors: []
            }
        },
        methods: {
            async changeSubscription(index, channelId) {
                const data = await profileApi.changeSubscription(channelId)

                this.authors[index].subscribed = !this.authors[index].subscribed

                // const author = this.authors[index]
                // this.authors = [
                //     ...this.authors.slice(0, index),
                //     {
                //         ...author,
                //         subscribed: !author.subscribed
                //     },
                //     ...this.authors.slice(index + 1)
                // ]
            }
        },
        async beforeMount() {
            const respAuthors = await profileApi.all()
            let authorsList = await respAuthors.json()

            const respSubscriptions = await profileApi.subscriptionsList()
            const userSubscriptions = await respSubscriptions.json()

            let subscriptionsSet = new Set()
            userSubscriptions.forEach(pair => subscriptionsSet.add(pair.channel.id))

            authorsList.forEach(author => {
                author.subscribed = subscriptionsSet.has(author.id)
            })

            this.authors = authorsList
        }
    }
</script>

<style scoped>
</style>