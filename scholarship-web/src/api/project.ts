/**
 * 科研项目相关 API 接口
 */
import request from '@/utils/request'

/**
 * 科研项目
 */
export interface ResearchProject {
  id?: number
  studentId: number
  studentName?: string
  projectName: string
  projectType: number
  projectNo?: string
  level?: number
  startDate?: string
  endDate?: string
  funding?: number
  role?: number
  status?: number
  createTime?: string
}

/**
 * 分页查询科研项目参数
 */
export interface ProjectPageParams extends API.PageParams {
  studentId?: number
  projectType?: number
  status?: number
  keyword?: string
}

/**
 * 分页查询科研项目
 * @param params - 查询参数
 * @returns 分页响应
 */
export function getProjectPage(
  params: ProjectPageParams
): Promise<API.Response<API.PageResponse<ResearchProject>>> {
  return request({
    url: '/research-project/page',
    method: 'get',
    params
  })
}

/**
 * 获取项目详情
 * @param id - 项目 ID
 * @returns Promise
 */
export function getProjectDetail(id: number): Promise<API.Response<ResearchProject>> {
  return request({
    url: `/research-project/${id}`,
    method: 'get'
  })
}

/**
 * 新增项目
 * @param data - 项目信息
 * @returns Promise
 */
export function addProject(data: Omit<ResearchProject, 'id'>): Promise<API.Response<null>> {
  return request({
    url: '/research-project',
    method: 'post',
    data
  })
}

/**
 * 更新项目
 * @param data - 项目信息（必须包含 id）
 * @returns Promise
 */
export function updateProject(
  data: Partial<ResearchProject> & { id: number }
): Promise<API.Response<null>> {
  return request({
    url: '/research-project',
    method: 'put',
    data
  })
}

/**
 * 删除项目
 * @param id - 项目 ID
 * @returns Promise
 */
export function deleteProject(id: number): Promise<API.Response<null>> {
  return request({
    url: `/research-project/${id}`,
    method: 'delete'
  })
}

export default {
  getProjectPage,
  getProjectDetail,
  addProject,
  updateProject,
  deleteProject
}
