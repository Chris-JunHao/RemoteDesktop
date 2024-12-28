package cn.edu.zjut.dockermanager.controller;

import cn.edu.zjut.dockermanager.base.ResultBean;
import cn.edu.zjut.dockermanager.entity.Log;
import cn.edu.zjut.dockermanager.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("log")
public class LogController {

    @Autowired
    private LogService logService;

    // 获取操作日志列表，支持分页
    @GetMapping("getLogs")
    public ResultBean getLogs(
            @RequestParam(defaultValue = "10") int limit, // 默认每页10条
            @RequestParam(defaultValue = "0") int offset // 默认从第0条开始
    ) {
        // 获取日志条目
        List<Log> logs = logService.getLogs(limit, offset);

        // 获取总的日志条数（用于分页）
        int total = logService.countLogs();

        // 构建一个返回数据对象（包含 items 和 total）
        Map<String, Object> response = new HashMap<>();
        response.put("items", logs);
        response.put("total", total);

        return new ResultBean<>(response);
    }
}
