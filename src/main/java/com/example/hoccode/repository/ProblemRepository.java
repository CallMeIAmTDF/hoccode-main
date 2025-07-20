package com.example.hoccode.repository;

import com.example.hoccode.dto.projection.CategoryCountProjection;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Integer>, JpaSpecificationExecutor<Problem> {
    Problem findFirstById(Integer id);

    Optional<Problem> findById(Integer id);

    @Query(
            """
                SELECT p FROM Problem p LEFT JOIN Submission s ON s.pid = p.id AND s.status = 'ACCEPTED' GROUP BY p.id ORDER BY COUNT(s.id) ASC LIMIT 1
            """
    )
    Problem getDailyProblem();

    @Query("SELECT c AS category, COUNT(p) AS count " +
            "FROM Problem p JOIN p.categories c " +
            "GROUP BY c ORDER BY COUNT(p) DESC")
    List<CategoryCountProjection> countProblemsByCategory();
}
