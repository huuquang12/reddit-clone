package com.example.springredditclone.service;

import com.example.springredditclone.dto.VoteDto;
import com.example.springredditclone.exceptions.PostNotFoundException;
import com.example.springredditclone.exceptions.SpringRedditException;
import com.example.springredditclone.model.Post;
import com.example.springredditclone.model.Vote;
import com.example.springredditclone.model.VoteType;
import com.example.springredditclone.repository.PostRepository;
import com.example.springredditclone.repository.VoteRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.springredditclone.model.VoteType.*;

@Service
@AllArgsConstructor
public class VoteService {

    private final PostRepository postRepository;
    private final VoteRepository voteRepository;
    private final AuthService authService;

    public void vote(VoteDto voteDto) {
        Post post = postRepository.findById(voteDto.getPostId())
                .orElseThrow(() ->
                            new PostNotFoundException("Post not found with ID: " + voteDto.getPostId().toString())
                        );
        Optional<Vote> voteByPostAndUser = voteRepository.
                findTopByPostAndUserOrderByVoteIdDesc(post, authService.getCurrentUSer());
        if (voteByPostAndUser.isPresent() &&
            voteByPostAndUser.get().getVoteType()
                    .equals(voteDto.getVoteType())
        ) {
            throw new SpringRedditException("You have already " + voteDto.getVoteType()
                        + "'d for this post");
        }
        if (UPVOTE.equals(voteDto.getVoteType())) {
            post.setVoteCount(post.getVoteCount() + 1);
        } else {
            post.setVoteCount(post.getVoteCount() - 1);
        }
        voteRepository.save(maptoVote(voteDto, post));
        postRepository.save(post);
    }

    private Vote maptoVote(VoteDto voteDto, Post post) {
        return Vote.builder()
                .voteType(voteDto.getVoteType())
                .post(post)
                .user(authService.getCurrentUSer())
                .build();
    }
}
