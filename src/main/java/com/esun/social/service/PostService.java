package com.esun.social.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class PostService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- 發文功能 ---

    public void createPost(String phone, String content) {
        String sql = "INSERT INTO posts (user_id, content) SELECT user_id, ? FROM users WHERE phone = ?";
        jdbcTemplate.update(sql, content, phone);
    }

 // --- 取得所有發文與對應的留言 ---
    public List<Map<String, Object>> getAllPosts() {
        // 1. 先撈出所有的貼文 (包含發文者名稱)
        String sql = "SELECT p.*, u.user_name FROM posts p JOIN users u ON p.user_id = u.user_id ORDER BY p.created_at DESC";
        List<Map<String, Object>> posts = jdbcTemplate.queryForList(sql);

        // 2. 針對每一篇貼文，去資料庫把屬於它的「留言」撈出來
        for (Map<String, Object> post : posts) {
            // 取得這篇文的 post_id (因為資料庫欄位型態差異，用 Number 轉 int 最安全)
            Number postId = (Number) post.get("post_id"); 
            
            // 撈取該貼文的所有留言 (依照時間舊到新排序)
            String commentSql = "SELECT c.*, u.user_name FROM comments c JOIN users u ON c.user_id = u.user_id WHERE c.post_id = ? ORDER BY c.created_at ASC";
            List<Map<String, Object>> comments = jdbcTemplate.queryForList(commentSql, postId.intValue());
            
            // 3. 把撈到的留言清單，當作一個屬性塞進這篇貼文裡
            post.put("comments", comments);
        }
        
        return posts;
    }

    public void updatePost(int postId, String phone, String content) throws Exception {
        String sql = "UPDATE posts SET content = ? WHERE post_id = ? AND user_id = (SELECT user_id FROM users WHERE phone = ?)";
        int rows = jdbcTemplate.update(sql, content, postId, phone);
        if (rows == 0) throw new Exception("修改失敗：無此權限或貼文不存在");
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePost(int postId, String phone) throws Exception {
        // 因為有建立 ON DELETE CASCADE，刪除 post 會自動刪除相關 comments
        String sql = "DELETE FROM posts WHERE post_id = ? AND user_id = (SELECT user_id FROM users WHERE phone = ?)";
        int rows = jdbcTemplate.update(sql, postId, phone);
        if (rows == 0) throw new Exception("刪除失敗：無此權限或貼文不存在");
    }

    // --- 留言功能 ---

    @Transactional(rollbackFor = Exception.class)
    public void addComment(int postId, String phone, String content) throws Exception {
        // 檢查貼文是否存在
        String checkSql = "SELECT COUNT(*) FROM posts WHERE post_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, postId);
        
        if (count == null || count == 0) {
            throw new Exception("發文不存在，無法留言");
        }

        String sql = "INSERT INTO comments (post_id, user_id, content) " +
                     "SELECT ?, user_id, ? FROM users WHERE phone = ?";
        jdbcTemplate.update(sql, postId, content, phone);
    }

}