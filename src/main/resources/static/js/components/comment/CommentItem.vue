<template>
    <v-list-tile>
        <user-link :user="comment.author"></user-link>
        <v-list-tile-content>
            <v-list-tile-title>{{comment.text}}</v-list-tile-title>
        </v-list-tile-content>
    </v-list-tile>
</template>

<script>
    import UserLink from "components/UserLink.vue"

    export default {
        name: 'CommentItem',
        props: ['comment'],
        components: {UserLink},
    }
</script>

<style scoped>

</style>
