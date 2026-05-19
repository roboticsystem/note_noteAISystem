package com.noteaiBackend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.noteaiBackend.dto.CreateNoteByUserIdDTO;
import com.noteaiBackend.dto.CreateNoteVersionDTO;
import com.noteaiBackend.dto.NoteDetailDTO;
import com.noteaiBackend.dto.NoteFindByTaskIdWithCommentDTO;
import com.noteaiBackend.dto.PublicNoteDTO;
import com.noteaiBackend.dto.UpdateNoteDTO;
import com.noteaiBackend.entity.Note;
import com.noteaiBackend.entity.NoteAtt;
import com.noteaiBackend.entity.NoteTag;
import com.noteaiBackend.entity.Tag;
import com.noteaiBackend.repository.NoteAttRepository;
import com.noteaiBackend.repository.NoteRepository;
import com.noteaiBackend.repository.NoteTagRepository;
import com.noteaiBackend.repository.TagRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository repository;
    private final NoteVersionService noteVersionService;
    private final TagRepository tagRepository;
    private final NoteTagRepository noteTagRepository;
    private final NoteAttRepository noteAttRepository;

    public List<Note> findAll() {
        return repository.findAll();
    }

    public Optional<Note> findById(Integer id) {
        return repository.findById(id);
    }

    public NoteDetailDTO getNoteDetail(Integer id) {
        Note note = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));
        
        List<Object[]> tagResults = repository.findTagsByNoteId(id);
        List<Tag> tags = tagResults.stream().map(row -> {
            Tag tag = new Tag();
            tag.setId(row[0] != null ? ((Number) row[0]).intValue() : null);
            tag.setName((String) row[1]);
            tag.setDescription((String) row[2]);
            tag.setCategory((String) row[3]);
            tag.setColor((String) row[4]);
            tag.setUsageCount(row[5] != null ? ((Number) row[5]).intValue() : 0);
            return tag;
        }).collect(Collectors.toList());

        List<NoteAtt> attachments = noteAttRepository.findByNoteId(id);
        
        NoteDetailDTO dto = new NoteDetailDTO();
        dto.setNote(note);
        dto.setTags(tags);
        dto.setAttachments(attachments);
        return dto;
    }

    public Note save(Note entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public List<NoteFindByTaskIdWithCommentDTO> findByTaskIdWithComment(Integer id) {
        return repository.findByTaskIdWithComment(id);
    }

    @Transactional
    public Note createByTaskId(Integer taskId, CreateNoteByUserIdDTO createNoteByUserIdDTO) {
        Note entity = new Note();
        entity.setTitle(createNoteByUserIdDTO.getTitle());
        entity.setContent(createNoteByUserIdDTO.getContent());
        entity.setUserId(createNoteByUserIdDTO.getUserId());
        if(taskId != null) {
            entity.setTaskId(taskId);
        }
        entity.setVision((byte) 1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setStatus(Note.STATUS_BLOCKED); // 新笔记默认为屏蔽状态（草稿）
        entity.setTag(createNoteByUserIdDTO.getTag());
        entity.setSummary(createNoteByUserIdDTO.getSummary());
        entity.setIsScore(0);
        Note savedNote = repository.save(entity);
        
        // Sync tags to tag and note_tag tables
        syncTags(savedNote.getId(), savedNote.getTag());
        
        // Sync attachments
        syncAttachments(savedNote.getId(), createNoteByUserIdDTO.getAttachments());
        
        return savedNote;
    }

    /**
     * 更新笔记
     * @param noteId 笔记ID
     * @param updateNoteDTO 更新数据
     * @return 更新后的笔记
     */
    @Transactional
    public Note updateNote(Integer noteId, UpdateNoteDTO updateNoteDTO) {
        Note existingNote = repository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));
        
        // 更新笔记内容
        existingNote.setTitle(updateNoteDTO.getTitle());
        existingNote.setContent(updateNoteDTO.getContent());
        existingNote.setTag(updateNoteDTO.getTag());
        existingNote.setSummary(updateNoteDTO.getSummary());
        existingNote.setUpdateTime(LocalDateTime.now());
        
        // 如果提供了vision字段，则更新
        if (updateNoteDTO.getVision() != null) {
            existingNote.setVision(updateNoteDTO.getVision());
        }
        
        Note savedNote = repository.save(existingNote);
        
        // Sync tags to tag and note_tag tables
        syncTags(savedNote.getId(), savedNote.getTag());

        // Sync attachments
        syncAttachments(savedNote.getId(), updateNoteDTO.getAttachments());

        // 在更新后创建版本（保存更新后的版本作为当前最新快照）
        CreateNoteVersionDTO versionDTO = CreateNoteVersionDTO.builder()
                .noteId(noteId)
                .userId(existingNote.getUserId())
                .changeSummary("自动保存版本")
                .build();
        noteVersionService.createVersion(versionDTO);
        
        return savedNote;
    }

    private void syncAttachments(Integer noteId, List<NoteAtt> attachments) {
        // Remove existing attachments records
        List<NoteAtt> existing = noteAttRepository.findByNoteId(noteId);
        noteAttRepository.deleteAll(existing);
        
        if (attachments != null) {
            for (NoteAtt att : attachments) {
                NoteAtt newAtt = new NoteAtt();
                newAtt.setNoteId(noteId);
                newAtt.setFileName(att.getFileName());
                newAtt.setFileUrl(att.getFileUrl());
                newAtt.setFileSize(att.getFileSize());
                noteAttRepository.save(newAtt);
            }
        }
    }

    private void syncTags(Integer noteId, String tagNames) {
        // Remove existing tags
        noteTagRepository.deleteByNoteId(noteId);
        
        if (tagNames == null || tagNames.trim().isEmpty()) {
            return;
        }
        
        String[] names = tagNames.split(",");
        for (String name : names) {
            name = name.trim();
            if (name.isEmpty()) continue;
            
            final String finalName = name;
            Tag tag = tagRepository.findByName(finalName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(finalName);
                        newTag.setCreateTime(LocalDateTime.now());
                        newTag.setUpdateTime(LocalDateTime.now());
                        return tagRepository.save(newTag);
                    });
            
            NoteTag noteTag = new NoteTag();
            noteTag.setNoteId(noteId);
            noteTag.setTagId(tag.getId());
            noteTag.setCreateTime(LocalDateTime.now());
            noteTagRepository.save(noteTag);
        }
    }

    /**
     * 获取公开笔记列表
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 包含笔记列表和总数的Map
     */
    public PublicNotesResponse getPublicNotes(int page, int size) {
        int offset = (page - 1) * size;
        List<Object[]> results = repository.findPublicNotes(size, offset);
        long total = repository.countPublicNotes();
        
        List<PublicNoteDTO> notes = results.stream().map(this::mapToPublicNoteDTO).collect(Collectors.toList());
        
        return new PublicNotesResponse(notes, total);
    }

    /**
     * 将查询结果映射到PublicNoteDTO
     */
    private PublicNoteDTO mapToPublicNoteDTO(Object[] result) {
        PublicNoteDTO dto = new PublicNoteDTO();
        
        // 注意：查询结果的列顺序必须与SELECT语句中的顺序一致
        if (result[0] != null) dto.setId(((Number) result[0]).intValue());
        if (result[1] != null) dto.setTitle((String) result[1]);
        if (result[2] != null) dto.setContent((String) result[2]);
        if (result[3] != null) dto.setSummary((String) result[3]);
        if (result[4] != null) dto.setUserId(((Number) result[4]).intValue());
        if (result[5] != null) dto.setAuthorName((String) result[5]);
        if (result[6] != null) dto.setAuthorAvatar((String) result[6]);
        if (result[7] != null) dto.setTaskId(((Number) result[7]).intValue());
        if (result[8] != null) dto.setCourseId(((Number) result[8]).intValue());
        if (result[9] != null) dto.setCourseName((String) result[9]);
        if (result[10] != null) dto.setVision(((Number) result[10]).byteValue());
        if (result[11] != null) dto.setCreateTime((LocalDateTime) result[11]);
        if (result[12] != null) dto.setUpdateTime((LocalDateTime) result[12]);
        if (result[13] != null) dto.setStatus(((Number) result[13]).intValue());
        if (result[14] != null) dto.setTag((String) result[14]);
        if (result[15] != null) dto.setIsScore(((Number) result[15]).intValue());
        if (result[16] != null) dto.setLikes(((Number) result[16]).intValue());
        if (result[17] != null) dto.setViews(((Number) result[17]).intValue());
        if (result[18] != null) dto.setComments(((Number) result[18]).intValue());
        
        return dto;
    }

    /**
     * 获取课程优秀笔记
     * @param classId 课程ID
     * @return 优秀笔记列表
     */
    public List<PublicNoteDTO> getExcellentNotesByClassId(Integer classId) {
        List<Object[]> results = repository.findExcellentNotesByClassId(classId);
        return results.stream().map(this::mapToPublicNoteDTO).collect(Collectors.toList());
    }

    /**
     * 获取用户笔记列表（分页）
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 包含笔记列表和总数的响应
     */
    public PublicNotesResponse getNotesByUserId(Integer userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Object[]> results = repository.findNotesByUserId(userId, size, offset);
        long total = repository.countNotesByUserId(userId);
        
        List<PublicNoteDTO> notes = results.stream().map(this::mapToPublicNoteDTO).collect(Collectors.toList());
        
        return new PublicNotesResponse(notes, total);
    }

    public Note updateNoteIsScore(Integer id) {
        Note existingNote = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        Integer isScore = existingNote.getIsScore();
        if(isScore == null || isScore == 0) {
            isScore = 1;
        }
        else {
            isScore = 0;
        }
        existingNote.setIsScore(isScore);
        return repository.save(existingNote);
    }

    /**
     * 更新笔记状态（屏蔽/取消屏蔽）
     * @param id 笔记ID
     * @param status 状态值
     * @return 更新后的笔记
     */
    public Note updateStatus(Integer id, Integer status) {
        Note existingNote = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));
        
        existingNote.setStatus(status);
        existingNote.setUpdateTime(LocalDateTime.now());
        return repository.save(existingNote);
    }

    public List<Object[]> findHotNotes(int limit) {
        return repository.findHotNotes(limit);
    }

    public Page<Object[]> findAdminNotes(String query, Pageable pageable) {
        return repository.findAdminNotes(query, pageable);
    }

    /**
     * 管理员获取笔记列表（已组装好Map格式）
     * 将查询结果组装成前端需要的格式，包含id、title、author、courseName、isPublic、reportCount、content
     *
     * @param query 搜索关键词（可选）
     * @param pageable 分页参数
     * @return 包含list和total的Map
     */
    public Map<String, Object> getAdminNoteList(String query, Pageable pageable) {
        Page<Object[]> notePage = repository.findAdminNotes(query, pageable);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : notePage.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row[0]);
            item.put("title", row[1]);
            item.put("author", row[2]);
            item.put("courseName", row[3]);
            item.put("vision", row[4]);
            item.put("isPublic", row[4] != null && ((Number) row[4]).byteValue() == 2);
            item.put("reportCount", row[5]);
            item.put("content", row[6]);
            list.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", notePage.getTotalElements());
        return data;
    }

    /**
     * 公开笔记响应类
     */
    @Setter
    @Getter
    public static class PublicNotesResponse {
        private List<PublicNoteDTO> notes;
        private long total;

        public PublicNotesResponse(List<PublicNoteDTO> notes, long total) {
            this.notes = notes;
            this.total = total;
        }

    }
}
