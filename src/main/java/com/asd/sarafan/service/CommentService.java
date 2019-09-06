package com.asd.sarafan.service;

import com.asd.sarafan.domain.*;
import com.asd.sarafan.dto.EventType;
import com.asd.sarafan.dto.ObjectType;
import com.asd.sarafan.repo.CommentRepo;
import com.asd.sarafan.repo.MessageRepo;
import com.asd.sarafan.repo.UserSubscriptionRepo;
import com.asd.sarafan.util.WsSender;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CommentService {
    private final CommentRepo commentRepo;
    private final MessageRepo messageRepo;
    private final UserSubscriptionRepo userSubscriptionRepo;
    private final TriConsumer<String, EventType, Comment> wsSender;

    @Autowired
    public CommentService(CommentRepo commentRepo, MessageRepo messageRepo, UserSubscriptionRepo userSubscriptionRepo, WsSender wsSender) {
        this.commentRepo = commentRepo;
        this.messageRepo = messageRepo;
        this.userSubscriptionRepo = userSubscriptionRepo;
        this.wsSender = wsSender.getSender(ObjectType.COMMENT, Views.FullComment.class);
    }

    public Comment create(Comment comment, User author) {
        comment.setAuthor(author);
        Comment commentFromDb = commentRepo.save(comment);

        Message parentMessage = messageRepo.getOne(commentFromDb.getMessage().getId());
        User authorOfParentMessage = parentMessage.getAuthor();

        // Send created comment via websocket to the author of parent message
        wsSender.accept(authorOfParentMessage.toString(),  EventType.CREATE, commentFromDb);

        // Send created message via websocket to every user that can view parent message
        for (UserSubscription subscription : userSubscriptionRepo.findByChannel(authorOfParentMessage)) {
            if (subscription.isActive()) {
                wsSender.accept(subscription.getSubscriber().toString(),  EventType.CREATE, commentFromDb);
            }
        }

        return commentFromDb;
    }
}
