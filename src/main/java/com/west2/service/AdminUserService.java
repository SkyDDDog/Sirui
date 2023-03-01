package com.west2.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.west2.entity.AdminUser;
import com.west2.entity.ProjectShare;
import com.west2.entity.vo.AdminUserProjectVO;
import com.west2.entity.vo.ProjectShowVO;
import com.west2.mapper.AdminUserMapper;
import com.west2.utils.Digests;
import com.west2.utils.EncodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AdminUserService extends CrudService<AdminUserMapper, AdminUser> {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectShareService projectShareService;

    /**
     * @desc    密码校验
     * @param plainPassword 明文密码
     * @param password  密文密码
     * @return
     */
    public static boolean validatePassword(String plainPassword, String password) {
        String plain = EncodeUtil.unescapeHtml(plainPassword);
        byte[] salt = EncodeUtil.decodeHex(password.substring(0, 16));
        byte[] hashPassword = Digests.sha1(plain.getBytes(), salt, 1024);
        return password.equals(EncodeUtil.encodeHex(salt) + EncodeUtil.encodeHex(hashPassword));
    }

    /**
     * @desc 密码加密
     * @param plainPassword 明文密码
     * @return  密文密码
     */
    public static String entryptPassword(String plainPassword) {
        String plain = EncodeUtil.unescapeHtml(plainPassword);
        byte[] salt = Digests.generateSalt(8);
        byte[] hashPassword = Digests.sha1(plain.getBytes(), salt, 1024);
        return EncodeUtil.encodeHex(salt) + EncodeUtil.encodeHex(hashPassword);
    }

    /**
     * @desc 通过用户名获取用户信息
     * @param username  用户名
     * @return
     */
    public AdminUser getByLoginUsername(String username) {
        QueryWrapper<AdminUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        List<AdminUser> list = this.findList(wrapper);
        if (list.size()==1) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * @desc 获取子用户场地
     * @param pageNum   第?页
     * @param pageSize  一页?条数据
     * @return
     */
    public List<AdminUserProjectVO> getSubUserProject(Integer pageNum, Integer pageSize) {
        QueryWrapper<AdminUser> wrapper = new QueryWrapper<>();
        wrapper.eq("role", AdminUser.ROLE_SUBACCOUNT);
        wrapper.last(" limit " + (pageNum - 1) * pageSize + " , " + pageSize);
        List<AdminUser> userList = this.findList(wrapper);
        ArrayList<AdminUserProjectVO> result = new ArrayList<>(userList.size());
        for (AdminUser adminUser : userList) {
            AdminUserProjectVO vo = new AdminUserProjectVO();
            BeanUtils.copyProperties(adminUser, vo);
            QueryWrapper<ProjectShare> projectWrapper = new QueryWrapper<>();
            projectWrapper.eq("user_id", adminUser.getId());
            List<ProjectShare> shareList = projectShareService.findList(projectWrapper);
            ArrayList<ProjectShowVO> list = new ArrayList<>(pageSize);
            for (ProjectShare projectShare : shareList) {
                ProjectShowVO project = projectService.getProjectShowById(projectShare.getProjectId());
                list.add(project);
            }
            vo.setProjectList(list);
            result.add(vo);
        }
        return result;
    }


}
