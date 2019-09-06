import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'


let stompClient = null
const handlers = []

export function connect() {
    stompClient = Stomp.over(function () {
        return new SockJS('/sarafan-websocket')
    })
    stompClient.debug = () => {}
    stompClient.connect({}, frame => {
        stompClient.subscribe('/user/queue/reply', message => {
            handlers.forEach(handler => handler(JSON.parse(message.body)))
        })
    })
}

export function addHandler(handler) {
    handlers.push(handler)
}

export function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect()
    }
    console.log("Disconnected")
}
