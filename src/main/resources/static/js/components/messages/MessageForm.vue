<template>
    <v-layout row>
        <v-text-field
                label="New message"
                placeholder="Write something"
                v-model="text"
                @keyup.enter="save"
        />
        <v-btn @click="save">
            Save
        </v-btn>
    </v-layout>
</template>

<script>
    import { mapActions } from 'vuex'

    export default {
        props: ['messageAttr'],
        data() {
            return {
                text: '',
                id: ''
            }
        },
        watch: {
            messageAttr(newVal, oldVal) {
                this.text = newVal.text
                this.id = newVal.id
            }
        },
        methods: {
            ...mapActions(['addMessageAction', 'updateMessageAction']),
            save() {
                if (this.id) {
                    const message = {
                        id: this.id,
                        text: this.text
                    }
                    this.updateMessageAction(message)
                } else {
                    // fixing Jackson deserialization (id can't be == "")
                    const message = {
                        text: this.text
                    }
                    this.addMessageAction(message)
                }

                this.text = ''
                this.id = ''
            }
        }
    }
</script>

<style>

</style>
