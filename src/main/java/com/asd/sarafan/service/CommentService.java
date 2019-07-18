package com.asd.sarafan.service;

import com.asd.sarafan.domain.Comment;
import com.asd.sarafan.domain.User;
import com.asd.sarafan.domain.Views;
import com.asd.sarafan.dto.EventType;
import com.asd.sarafan.dto.ObjectType;
import com.asd.sarafan.repo.CommentRepo;
import com.asd.sarafan.util.WsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;


@Service
public class CommentService {
    private final CommentRepo commentRepo;
    private final BiConsumer<EventType, Comment> wsSender;

    @Autowired
    public CommentService(CommentRepo commentRepo, WsSender wsSender) {
        this.commentRepo = commentRepo;
        this.wsSender = wsSender.getSender(ObjectType.COMMENT, Views.FullComment.class);
    }

    public Comment create(Comment comment, User author) {
        comment.setAuthor(author);
        Comment commentFromDb = commentRepo.save(comment);

        wsSender.accept(EventType.CREATE, commentFromDb);

        return commentFromDb;
    }
}
