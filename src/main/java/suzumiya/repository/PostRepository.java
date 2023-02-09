package suzumiya.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import suzumiya.model.pojo.Post;

@Repository
public interface PostRepository extends ElasticsearchRepository<Post, Long> {
}
