<template>
  <div class="achievements-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">科研成果管理</h2>
        <p class="page-subtitle">按成果类型分别维护论文、专利、项目和竞赛记录。</p>
      </div>
      <el-button type="primary" :disabled="!isTypeEditable" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加{{ activeTypeLabel }}
      </el-button>
    </div>

    <el-tabs v-model="activeType" @tab-change="handleTypeChange">
      <el-tab-pane
        v-for="item in achievementTypeOptions"
        :key="item.value"
        :label="item.label"
        :name="item.value"
      />
    </el-tabs>

    <el-form :inline="true" class="search-form">
      <el-form-item label="审核状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
          <el-option
            v-for="item in currentStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <template v-if="activeType === 'paper'">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="title" label="论文标题" min-width="220" />
        <el-table-column prop="journalName" label="期刊名称" min-width="180" />
        <el-table-column label="作者排名" width="120">
          <template #default="{ row }">
            {{ getAuthorRankLabel(row.authorRank) }}
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getRowStatusType(row)">
              {{ getRowStatusLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </template>

      <template v-else-if="activeType === 'patent'">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="patentName" label="专利名称" min-width="220" />
        <el-table-column prop="patentNo" label="专利号" min-width="180" />
        <el-table-column label="专利类型" width="120">
          <template #default="{ row }">
            {{ getPatentTypeLabel(row.patentType) }}
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getRowStatusType(row)">
              {{ getRowStatusLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </template>

      <template v-else-if="activeType === 'project'">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="projectName" label="项目名称" min-width="220" />
        <el-table-column label="项目类型" width="120">
          <template #default="{ row }">
            {{ getProjectTypeLabel(row.projectType) }}
          </template>
        </el-table-column>
        <el-table-column label="项目角色" width="120">
          <template #default="{ row }">
            {{ getProjectRoleLabel(row.projectRole) }}
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getRowStatusType(row)">
              {{ getRowStatusLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </template>

      <template v-else>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="competitionName" label="竞赛名称" min-width="220" />
        <el-table-column label="竞赛级别" width="120">
          <template #default="{ row }">
            {{ getCompetitionLevelLabel(row.competitionLevel) }}
          </template>
        </el-table-column>
        <el-table-column label="获奖等级" width="120">
          <template #default="{ row }">
            {{ getCompetitionAwardLevelLabel(row.awardLevel) }}
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getRowStatusType(row)">
              {{ getRowStatusLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </template>

      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" :disabled="!canEditRow(row)" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button
            v-if="activeType === 'paper'"
            link
            type="danger"
            :disabled="!canDeleteRow(row)"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.current"
      v-model:page-size="queryParams.size"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleQuery"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" @close="handleDialogClose">
      <el-form ref="formRef" :model="currentFormModel" :rules="currentFormRules" label-width="120px">
        <template v-if="activeType === 'paper'">
          <el-form-item label="论文标题" prop="paperTitle">
            <el-input v-model="paperForm.paperTitle" placeholder="请输入论文标题" />
          </el-form-item>
          <el-form-item label="作者列表" prop="authors">
            <el-input v-model="paperForm.authors" placeholder="多个作者请用逗号分隔" />
          </el-form-item>
          <el-form-item label="作者排名" prop="authorRank">
            <el-select v-model="paperForm.authorRank" placeholder="请选择">
              <el-option
                v-for="item in authorRankOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="期刊名称" prop="journalName">
            <el-input v-model="paperForm.journalName" placeholder="请输入期刊名称" />
          </el-form-item>
          <el-form-item label="期刊级别" prop="journalLevel">
            <el-select v-model="paperForm.journalLevel" placeholder="请选择">
              <el-option
                v-for="item in journalLevelOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="影响因子" prop="impactFactor">
            <el-input-number v-model="paperForm.impactFactor" :min="0" :max="100" :precision="2" />
          </el-form-item>
          <el-form-item label="发表日期" prop="publicationDate">
            <el-date-picker
              v-model="paperForm.publicationDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </template>

        <template v-else-if="activeType === 'patent'">
          <el-form-item label="专利名称" prop="patentName">
            <el-input v-model="patentForm.patentName" placeholder="请输入专利名称" />
          </el-form-item>
          <el-form-item label="专利号" prop="patentNo">
            <el-input v-model="patentForm.patentNo" placeholder="请输入专利号" />
          </el-form-item>
          <el-form-item label="专利类型" prop="patentType">
            <el-select v-model="patentForm.patentType" placeholder="请选择">
              <el-option
                v-for="item in patentTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="申请人" prop="applicant">
            <el-input v-model="patentForm.applicant" placeholder="请输入申请人" />
          </el-form-item>
          <el-form-item label="发明人" prop="inventors">
            <el-input v-model="patentForm.inventors" placeholder="多个发明人请用逗号分隔" />
          </el-form-item>
          <el-form-item label="发明人排名" prop="inventorRank">
            <el-input-number v-model="patentForm.inventorRank" :min="1" :precision="0" controls-position="right" />
          </el-form-item>
          <el-form-item label="申请人排名" prop="applicantRank">
            <el-input-number v-model="patentForm.applicantRank" :min="1" :precision="0" controls-position="right" />
          </el-form-item>
          <el-form-item label="申请日期" prop="applicationDate">
            <el-date-picker
              v-model="patentForm.applicationDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="专利状态" prop="patentStatus">
            <el-select v-model="patentForm.patentStatus" placeholder="请选择">
              <el-option
                v-for="item in patentStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="patentForm.remark" type="textarea" :rows="3" placeholder="可选" />
          </el-form-item>
        </template>

        <template v-else-if="activeType === 'project'">
          <el-form-item label="项目名称" prop="projectName">
            <el-input v-model="projectForm.projectName" placeholder="请输入项目名称" />
          </el-form-item>
          <el-form-item label="项目类型" prop="projectType">
            <el-select v-model="projectForm.projectType" placeholder="请选择">
              <el-option
                v-for="item in projectTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="项目级别" prop="projectLevel">
            <el-select v-model="projectForm.projectLevel" placeholder="请选择">
              <el-option
                v-for="item in projectLevelOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="项目编号" prop="projectNo">
            <el-input v-model="projectForm.projectNo" placeholder="请输入项目编号" />
          </el-form-item>
          <el-form-item label="项目来源" prop="projectSource">
            <el-input v-model="projectForm.projectSource" placeholder="请输入项目来源" />
          </el-form-item>
          <el-form-item label="负责人" prop="leaderName">
            <el-input v-model="projectForm.leaderName" placeholder="请输入负责人姓名" />
          </el-form-item>
          <el-form-item label="成员排名" prop="memberRank">
            <el-input-number v-model="projectForm.memberRank" :min="1" :precision="0" controls-position="right" />
          </el-form-item>
          <el-form-item label="项目角色" prop="projectRole">
            <el-select v-model="projectForm.projectRole" placeholder="请选择">
              <el-option
                v-for="item in projectRoleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="参与人员" prop="participants">
            <el-input v-model="projectForm.participants" placeholder="多个成员请用逗号分隔" />
          </el-form-item>
          <el-form-item label="开始日期" prop="startDate">
            <el-date-picker
              v-model="projectForm.startDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="结束日期" prop="endDate">
            <el-date-picker
              v-model="projectForm.endDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="项目经费" prop="funding">
            <el-input-number v-model="projectForm.funding" :min="0" :precision="2" />
          </el-form-item>
          <el-form-item label="项目状态" prop="projectStatus">
            <el-select v-model="projectForm.projectStatus" placeholder="请选择">
              <el-option
                v-for="item in projectStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="projectForm.remark" type="textarea" :rows="3" placeholder="可选" />
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="竞赛名称" prop="competitionName">
            <el-input v-model="competitionForm.competitionName" placeholder="请输入竞赛名称" />
          </el-form-item>
          <el-form-item label="竞赛级别" prop="competitionLevel">
            <el-select v-model="competitionForm.competitionLevel" placeholder="请选择">
              <el-option
                v-for="item in competitionLevelOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="获奖等级" prop="awardLevel">
            <el-select v-model="competitionForm.awardLevel" placeholder="请选择">
              <el-option
                v-for="item in competitionAwardLevelOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="获奖名次" prop="awardRank">
            <el-input-number v-model="competitionForm.awardRank" :min="1" :precision="0" controls-position="right" />
          </el-form-item>
          <el-form-item label="获奖类型" prop="awardType">
            <el-select v-model="competitionForm.awardType" placeholder="请选择">
              <el-option
                v-for="item in competitionAwardTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="成员排名" prop="memberRank">
            <el-input-number
              v-model="competitionForm.memberRank"
              :min="1"
              :precision="0"
              controls-position="right"
            />
          </el-form-item>
          <el-form-item label="指导老师" prop="instructor">
            <el-input v-model="competitionForm.instructor" placeholder="请输入指导老师" />
          </el-form-item>
          <el-form-item label="颁发单位" prop="issuingUnit">
            <el-input v-model="competitionForm.issuingUnit" placeholder="请输入颁发单位" />
          </el-form-item>
          <el-form-item label="主办单位" prop="organizer">
            <el-input v-model="competitionForm.organizer" placeholder="请输入主办单位" />
          </el-form-item>
          <el-form-item label="团队成员" prop="teamMembers">
            <el-input v-model="competitionForm.teamMembers" placeholder="多个成员请用逗号分隔" />
          </el-form-item>
          <el-form-item label="获奖日期" prop="awardDate">
            <el-date-picker
              v-model="competitionForm.awardDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="competitionForm.remark" type="textarea" :rows="3" placeholder="可选" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" :title="`${activeTypeLabel}详情`" width="640px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <template v-if="activeType === 'paper'">
          <el-descriptions-item label="论文标题" :span="2">{{ currentPaperRow?.title || '-' }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentPaperRow?.authors || '-' }}</el-descriptions-item>
          <el-descriptions-item label="作者排名">{{ getAuthorRankLabel(currentPaperRow?.authorRank) }}</el-descriptions-item>
          <el-descriptions-item label="期刊名称">{{ currentPaperRow?.journalName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="期刊级别">{{ getJournalLevelLabel(currentPaperRow?.journalLevel) }}</el-descriptions-item>
          <el-descriptions-item label="影响因子">{{ currentPaperRow?.impactFactor ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="发表日期">{{ currentPaperRow?.publicationDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ getRowStatusLabel(currentRow) }}</el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2">{{ currentPaperRow?.reviewComment || '-' }}</el-descriptions-item>
        </template>

        <template v-else-if="activeType === 'patent'">
          <el-descriptions-item label="专利名称" :span="2">{{ currentPatentRow?.patentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专利号">{{ currentPatentRow?.patentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专利类型">{{ getPatentTypeLabel(currentPatentRow?.patentType) }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentPatentRow?.applicant || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发明人">{{ currentPatentRow?.inventors || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发明人排名">{{ currentPatentRow?.inventorRank || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请人排名">{{ currentPatentRow?.applicantRank || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请日期">{{ currentPatentRow?.applicationDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专利状态">{{ getPatentStatusLabel(currentPatentRow?.patentStatus) }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ getRowStatusLabel(currentRow) }}</el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2">{{ currentPatentRow?.auditComment || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentPatentRow?.remark || '-' }}</el-descriptions-item>
        </template>

        <template v-else-if="activeType === 'project'">
          <el-descriptions-item label="项目名称" :span="2">{{ currentProjectRow?.projectName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目类型">{{ getProjectTypeLabel(currentProjectRow?.projectType) }}</el-descriptions-item>
          <el-descriptions-item label="项目级别">{{ getProjectLevelLabel(currentProjectRow?.projectLevel) }}</el-descriptions-item>
          <el-descriptions-item label="项目编号">{{ currentProjectRow?.projectNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目来源">{{ currentProjectRow?.projectSource || '-' }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentProjectRow?.leaderName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="成员排名">{{ currentProjectRow?.memberRank || '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目角色">{{ getProjectRoleLabel(currentProjectRow?.projectRole) }}</el-descriptions-item>
          <el-descriptions-item label="参与人员" :span="2">{{ currentProjectRow?.participants || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ currentProjectRow?.startDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ currentProjectRow?.endDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目经费">{{ currentProjectRow?.funding ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目状态">{{ getProjectStatusLabel(currentProjectRow?.projectStatus) }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ getRowStatusLabel(currentRow) }}</el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2">{{ currentProjectRow?.auditComment || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentProjectRow?.remark || '-' }}</el-descriptions-item>
        </template>

        <template v-else>
          <el-descriptions-item label="竞赛名称" :span="2">{{ currentCompetitionRow?.competitionName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="竞赛级别">{{ getCompetitionLevelLabel(currentCompetitionRow?.competitionLevel) }}</el-descriptions-item>
          <el-descriptions-item label="获奖等级">{{ getCompetitionAwardLevelLabel(currentCompetitionRow?.awardLevel) }}</el-descriptions-item>
          <el-descriptions-item label="获奖名次">{{ currentCompetitionRow?.awardRank || '-' }}</el-descriptions-item>
          <el-descriptions-item label="获奖类型">{{ getCompetitionAwardTypeLabel(currentCompetitionRow?.awardType) }}</el-descriptions-item>
          <el-descriptions-item label="成员排名">{{ currentCompetitionRow?.memberRank || '-' }}</el-descriptions-item>
          <el-descriptions-item label="指导老师">{{ currentCompetitionRow?.instructor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="颁发单位">{{ currentCompetitionRow?.issuingUnit || '-' }}</el-descriptions-item>
          <el-descriptions-item label="主办单位">{{ currentCompetitionRow?.organizer || '-' }}</el-descriptions-item>
          <el-descriptions-item label="获奖日期">{{ currentCompetitionRow?.awardDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="团队成员" :span="2">{{ currentCompetitionRow?.teamMembers || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ getRowStatusLabel(currentRow) }}</el-descriptions-item>
          <el-descriptions-item label="审核意见">{{ currentCompetitionRow?.auditComment || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentCompetitionRow?.remark || '-' }}</el-descriptions-item>
        </template>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { deletePaper, getPaperPage, submitPaper, updatePaper, type Paper, type PaperPageParams } from '@/api/paper'
import {
  addPatent,
  getPatentPage,
  updatePatent,
  type PatentPageParams,
  type ResearchPatent
} from '@/api/patent'
import {
  addProject,
  getProjectPage,
  updateProject,
  type ProjectPageParams,
  type ResearchProject
} from '@/api/project'
import {
  addCompetition,
  getCompetitionPage,
  updateCompetition,
  type CompetitionAward,
  type CompetitionPageParams
} from '@/api/competition'
import {
  AUDIT_STATUS,
  AUDIT_STATUS_LABELS,
  AUDIT_STATUS_TYPES,
  AUTHOR_RANK,
  JOURNAL_LEVEL,
  extractPageData
} from '@/utils/helpers'

type AchievementType = 'paper' | 'patent' | 'project' | 'competition'
type NormalizedPaper = Paper & {
  title: string
  journalName: string
  publicationDate: string
}
type AuditedAchievementRow = ResearchPatent | ResearchProject | CompetitionAward
type AchievementRow = NormalizedPaper | AuditedAchievementRow

type Option = {
  label: string
  value: number
}

interface PaperForm {
  id: number | null
  paperTitle: string
  authors: string
  authorRank: number
  journalName: string
  journalLevel: number | null
  impactFactor: number | null
  publicationDate: string
}

interface PatentForm {
  id: number | null
  patentName: string
  patentNo: string
  patentType: number
  applicant: string
  inventors: string
  inventorRank: number | null
  applicantRank: number | null
  applicationDate: string
  patentStatus: number
  remark: string
}

interface ProjectForm {
  id: number | null
  projectName: string
  projectType: number
  projectLevel: number | null
  projectNo: string
  projectSource: string
  leaderName: string
  memberRank: number | null
  projectRole: number | null
  participants: string
  startDate: string
  endDate: string
  funding: number | null
  projectStatus: number | null
  remark: string
}

interface CompetitionForm {
  id: number | null
  competitionName: string
  competitionLevel: number | null
  awardLevel: number | null
  awardRank: number | null
  awardType: number | null
  memberRank: number | null
  instructor: string
  issuingUnit: string
  organizer: string
  teamMembers: string
  awardDate: string
  remark: string
}

const GENERIC_AUDIT_OPTIONS: Option[] = [
  { label: '待审核', value: 0 },
  { label: '审核通过', value: 1 },
  { label: '审核驳回', value: 2 }
]

const GENERIC_AUDIT_LABELS: Record<number, string> = {
  0: '待审核',
  1: '审核通过',
  2: '审核驳回'
}

const GENERIC_AUDIT_TYPES: Record<number, 'warning' | 'success' | 'danger' | 'info'> = {
  0: 'warning',
  1: 'success',
  2: 'danger'
}

const achievementTypeOptions: Array<{ label: string; value: AchievementType }> = [
  { label: '论文', value: 'paper' },
  { label: '专利', value: 'patent' },
  { label: '项目', value: 'project' },
  { label: '竞赛', value: 'competition' }
]

const authorRankOptions: Option[] = [
  { label: '第一作者', value: AUTHOR_RANK.FIRST },
  { label: '第二作者', value: AUTHOR_RANK.SECOND },
  { label: '通讯作者', value: AUTHOR_RANK.CORRESPONDING }
]

const journalLevelOptions: Option[] = [
  { label: 'SCI 一区', value: JOURNAL_LEVEL.SCI_Q1 },
  { label: 'SCI 二区', value: JOURNAL_LEVEL.SCI_Q2 },
  { label: 'SCI 三区', value: JOURNAL_LEVEL.SCI_Q3 },
  { label: 'SCI 四区', value: JOURNAL_LEVEL.SCI_Q4 },
  { label: 'EI', value: JOURNAL_LEVEL.EI },
  { label: '核心期刊', value: JOURNAL_LEVEL.CORE },
  { label: '普通期刊', value: JOURNAL_LEVEL.REGULAR }
]

const patentTypeOptions: Option[] = [
  { label: '发明专利', value: 1 },
  { label: '实用新型', value: 2 },
  { label: '外观设计', value: 3 }
]

const patentStatusOptions: Option[] = [
  { label: '申请中', value: 1 },
  { label: '已授权', value: 2 },
  { label: '已失效', value: 3 }
]

const projectTypeOptions: Option[] = [
  { label: '纵向项目', value: 1 },
  { label: '横向项目', value: 2 },
  { label: '校级项目', value: 3 }
]

const projectLevelOptions: Option[] = [
  { label: '国家级', value: 1 },
  { label: '省部级', value: 2 },
  { label: '市厅级', value: 3 },
  { label: '校级', value: 4 }
]

const projectRoleOptions: Option[] = [
  { label: '负责人', value: 1 },
  { label: '核心成员', value: 2 },
  { label: '参与成员', value: 3 }
]

const projectStatusOptions: Option[] = [
  { label: '立项中', value: 1 },
  { label: '进行中', value: 2 },
  { label: '已结题', value: 3 }
]

const competitionLevelOptions: Option[] = [
  { label: '国际级', value: 1 },
  { label: '国家级', value: 2 },
  { label: '省部级', value: 3 },
  { label: '校级', value: 4 }
]

const competitionAwardLevelOptions: Option[] = [
  { label: '特等奖', value: 1 },
  { label: '一等奖', value: 2 },
  { label: '二等奖', value: 3 },
  { label: '三等奖', value: 4 },
  { label: '优秀奖', value: 5 }
]

const competitionAwardTypeOptions: Option[] = [
  { label: '个人赛', value: 1 },
  { label: '团队赛', value: 2 }
]

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const activeType = ref<AchievementType>('paper')
const formRef = ref<FormInstance | null>(null)
const tableData = ref<AchievementRow[]>([])
const currentRow = ref<AchievementRow | null>(null)

const queryParams = reactive({
  current: 1,
  size: 10,
  status: undefined as number | undefined | ''
})

const paperForm = reactive<PaperForm>({
  id: null,
  paperTitle: '',
  authors: '',
  authorRank: AUTHOR_RANK.FIRST,
  journalName: '',
  journalLevel: null,
  impactFactor: null,
  publicationDate: ''
})

const patentForm = reactive<PatentForm>({
  id: null,
  patentName: '',
  patentNo: '',
  patentType: 1,
  applicant: '',
  inventors: '',
  inventorRank: 1,
  applicantRank: 1,
  applicationDate: '',
  patentStatus: 1,
  remark: ''
})

const projectForm = reactive<ProjectForm>({
  id: null,
  projectName: '',
  projectType: 1,
  projectLevel: 1,
  projectNo: '',
  projectSource: '',
  leaderName: '',
  memberRank: 1,
  projectRole: 1,
  participants: '',
  startDate: '',
  endDate: '',
  funding: null,
  projectStatus: 1,
  remark: ''
})

const competitionForm = reactive<CompetitionForm>({
  id: null,
  competitionName: '',
  competitionLevel: 1,
  awardLevel: 2,
  awardRank: 1,
  awardType: 1,
  memberRank: 1,
  instructor: '',
  issuingUnit: '',
  organizer: '',
  teamMembers: '',
  awardDate: '',
  remark: ''
})

const paperRules: FormRules<PaperForm> = {
  paperTitle: [{ required: true, message: '请输入论文标题', trigger: 'blur' }],
  authors: [{ required: true, message: '请输入作者列表', trigger: 'blur' }],
  authorRank: [{ required: true, message: '请选择作者排名', trigger: 'change' }],
  journalLevel: [{ required: true, message: '请选择期刊级别', trigger: 'change' }]
}

const patentRules: FormRules<PatentForm> = {
  patentName: [{ required: true, message: '请输入专利名称', trigger: 'blur' }],
  patentNo: [{ required: true, message: '请输入专利号', trigger: 'blur' }],
  patentType: [{ required: true, message: '请选择专利类型', trigger: 'change' }],
  applicant: [{ required: true, message: '请输入申请人', trigger: 'blur' }],
  inventors: [{ required: true, message: '请输入发明人', trigger: 'blur' }]
}

const projectRules: FormRules<ProjectForm> = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
  projectLevel: [{ required: true, message: '请选择项目级别', trigger: 'change' }]
}

const competitionRules: FormRules<CompetitionForm> = {
  competitionName: [{ required: true, message: '请输入竞赛名称', trigger: 'blur' }],
  competitionLevel: [{ required: true, message: '请选择竞赛级别', trigger: 'change' }],
  awardLevel: [{ required: true, message: '请选择获奖等级', trigger: 'change' }]
}

const activeTypeLabel = computed(() => achievementTypeOptions.find(item => item.value === activeType.value)?.label || '成果')

const isTypeEditable = computed(() => activeType.value === 'paper' || activeType.value === 'patent')

const currentStatusOptions = computed(() =>
  activeType.value === 'paper'
    ? [
        { label: AUDIT_STATUS_LABELS[AUDIT_STATUS.PENDING], value: AUDIT_STATUS.PENDING },
        { label: AUDIT_STATUS_LABELS[AUDIT_STATUS.TUTOR_APPROVED], value: AUDIT_STATUS.TUTOR_APPROVED },
        { label: AUDIT_STATUS_LABELS[AUDIT_STATUS.DEPARTMENT_APPROVED], value: AUDIT_STATUS.DEPARTMENT_APPROVED },
        { label: AUDIT_STATUS_LABELS[AUDIT_STATUS.REJECTED], value: AUDIT_STATUS.REJECTED }
      ]
    : GENERIC_AUDIT_OPTIONS
)

const currentFormModel = computed<Record<string, any>>(() => {
  switch (activeType.value) {
    case 'patent':
      return patentForm
    case 'project':
      return projectForm
    case 'competition':
      return competitionForm
    default:
      return paperForm
  }
})

const currentFormRules = computed<FormRules>(() => {
  switch (activeType.value) {
    case 'patent':
      return patentRules as FormRules
    case 'project':
      return projectRules as FormRules
    case 'competition':
      return competitionRules as FormRules
    default:
      return paperRules as FormRules
  }
})

const dialogTitle = computed(() => `${isEdit.value ? '编辑' : '添加'}${activeTypeLabel.value}`)
const currentPaperRow = computed(() => activeType.value === 'paper' ? (currentRow.value as NormalizedPaper | null) : null)
const currentPatentRow = computed(() => activeType.value === 'patent' ? (currentRow.value as ResearchPatent | null) : null)
const currentProjectRow = computed(() => activeType.value === 'project' ? (currentRow.value as ResearchProject | null) : null)
const currentCompetitionRow = computed(() => activeType.value === 'competition' ? (currentRow.value as CompetitionAward | null) : null)

function getOptionLabel(options: Option[], value?: number | null): string {
  return options.find(item => item.value === value)?.label || '-'
}

function normalizePaper(row: Paper): NormalizedPaper {
  return {
    ...row,
    title: row.title || row.paperTitle || '',
    journalName: row.journalName || row.journal || '',
    publicationDate: row.publicationDate || row.publishDate || row.date || ''
  }
}

function asPaperRow(row: AchievementRow): NormalizedPaper {
  return row as NormalizedPaper
}

function asPatentRow(row: AchievementRow): ResearchPatent {
  return row as ResearchPatent
}

function asProjectRow(row: AchievementRow): ResearchProject {
  return row as ResearchProject
}

function asCompetitionRow(row: AchievementRow): CompetitionAward {
  return row as CompetitionAward
}

function getAuthorRankLabel(value?: number): string {
  return getOptionLabel(authorRankOptions, value)
}

function getJournalLevelLabel(value?: number): string {
  return getOptionLabel(journalLevelOptions, value)
}

function getPatentTypeLabel(value?: number): string {
  return getOptionLabel(patentTypeOptions, value)
}

function getPatentStatusLabel(value?: number): string {
  return getOptionLabel(patentStatusOptions, value)
}

function getProjectTypeLabel(value?: number): string {
  return getOptionLabel(projectTypeOptions, value)
}

function getProjectLevelLabel(value?: number): string {
  return getOptionLabel(projectLevelOptions, value)
}

function getProjectRoleLabel(value?: number): string {
  return getOptionLabel(projectRoleOptions, value)
}

function getProjectStatusLabel(value?: number): string {
  return getOptionLabel(projectStatusOptions, value)
}

function getCompetitionLevelLabel(value?: number): string {
  return getOptionLabel(competitionLevelOptions, value)
}

function getCompetitionAwardLevelLabel(value?: number): string {
  return getOptionLabel(competitionAwardLevelOptions, value)
}

function getCompetitionAwardTypeLabel(value?: number): string {
  return getOptionLabel(competitionAwardTypeOptions, value)
}

function getRowStatusValue(row: AchievementRow): number | undefined {
  return activeType.value === 'paper' ? asPaperRow(row).status : (row as AuditedAchievementRow).auditStatus
}

function getRowStatusLabel(row: AchievementRow): string {
  const status = getRowStatusValue(row)
  if (status === undefined || status === null) return '-'
  return activeType.value === 'paper'
    ? AUDIT_STATUS_LABELS[status] || '未知状态'
    : GENERIC_AUDIT_LABELS[status] || '未知状态'
}

function getRowStatusType(row: AchievementRow): 'warning' | 'success' | 'danger' | 'info' | 'primary' {
  const status = getRowStatusValue(row)
  if (status === undefined || status === null) return 'info'
  return activeType.value === 'paper'
    ? AUDIT_STATUS_TYPES[status] || 'info'
    : GENERIC_AUDIT_TYPES[status] || 'info'
}

function canEditRow(row: AchievementRow): boolean {
  return isTypeEditable.value && getRowStatusValue(row) === AUDIT_STATUS.PENDING
}

function canDeleteRow(row: AchievementRow): boolean {
  return activeType.value === 'paper' && asPaperRow(row).status === AUDIT_STATUS.PENDING
}

function resetPaperForm() {
  Object.assign(paperForm, {
    id: null,
    paperTitle: '',
    authors: '',
    authorRank: AUTHOR_RANK.FIRST,
    journalName: '',
    journalLevel: null,
    impactFactor: null,
    publicationDate: ''
  })
}

function resetPatentForm() {
  Object.assign(patentForm, {
    id: null,
    patentName: '',
    patentNo: '',
    patentType: 1,
    applicant: '',
    inventors: '',
    inventorRank: 1,
    applicantRank: 1,
    applicationDate: '',
    patentStatus: 1,
    remark: ''
  })
}

function resetProjectForm() {
  Object.assign(projectForm, {
    id: null,
    projectName: '',
    projectType: 1,
    projectLevel: 1,
    projectNo: '',
    projectSource: '',
    leaderName: '',
    memberRank: 1,
    projectRole: 1,
    participants: '',
    startDate: '',
    endDate: '',
    funding: null,
    projectStatus: 1,
    remark: ''
  })
}

function resetCompetitionForm() {
  Object.assign(competitionForm, {
    id: null,
    competitionName: '',
    competitionLevel: 1,
    awardLevel: 2,
    awardRank: 1,
    awardType: 1,
    memberRank: 1,
    instructor: '',
    issuingUnit: '',
    organizer: '',
    teamMembers: '',
    awardDate: '',
    remark: ''
  })
}

function resetCurrentForm() {
  switch (activeType.value) {
    case 'patent':
      resetPatentForm()
      break
    case 'project':
      resetProjectForm()
      break
    case 'competition':
      resetCompetitionForm()
      break
    default:
      resetPaperForm()
  }
}

async function fetchTableData(): Promise<void> {
  loading.value = true
  try {
    if (activeType.value === 'paper') {
      const response = await getPaperPage({
        current: queryParams.current,
        size: queryParams.size,
        status: queryParams.status === '' ? undefined : (queryParams.status as PaperPageParams['status'])
      })
      const pageData = extractPageData<Paper>(response)
      tableData.value = (pageData?.records || []).map(normalizePaper)
      total.value = pageData?.total || 0
      return
    }

    if (activeType.value === 'patent') {
      const response = await getPatentPage({
        current: queryParams.current,
        size: queryParams.size,
        auditStatus: queryParams.status === '' ? undefined : (queryParams.status as PatentPageParams['auditStatus'])
      })
      const pageData = extractPageData<ResearchPatent>(response)
      tableData.value = pageData?.records || []
      total.value = pageData?.total || 0
      return
    }

    if (activeType.value === 'project') {
      const response = await getProjectPage({
        current: queryParams.current,
        size: queryParams.size,
        auditStatus: queryParams.status === '' ? undefined : (queryParams.status as ProjectPageParams['auditStatus'])
      })
      const pageData = extractPageData<ResearchProject>(response)
      tableData.value = pageData?.records || []
      total.value = pageData?.total || 0
      return
    }

    const response = await getCompetitionPage({
      current: queryParams.current,
      size: queryParams.size,
      auditStatus: queryParams.status === '' ? undefined : (queryParams.status as CompetitionPageParams['auditStatus'])
    })
    const pageData = extractPageData<CompetitionAward>(response)
    tableData.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch (error) {
    console.error('加载科研成果失败:', error)
  } finally {
    loading.value = false
  }
}

function syncRouteQuery(action?: 'add') {
  const query: Record<string, string> = { type: activeType.value }
  if (action) query.action = action
  router.replace({ path: '/app/student/achievements', query })
}

function handleTypeChange(name: string | number) {
  activeType.value = name as AchievementType
  queryParams.current = 1
  queryParams.status = undefined
  syncRouteQuery()
  fetchTableData()
}

function handleQuery(): void {
  queryParams.current = 1
  fetchTableData()
}

function handleReset(): void {
  queryParams.status = undefined
  queryParams.current = 1
  fetchTableData()
}

function handleSizeChange(): void {
  queryParams.current = 1
  fetchTableData()
}

function handleAdd(): void {
  if (!isTypeEditable.value) return
  isEdit.value = false
  resetCurrentForm()
  formRef.value?.clearValidate()
  dialogVisible.value = true
  syncRouteQuery('add')
}

function handleView(row: AchievementRow): void {
  currentRow.value = row
  detailVisible.value = true
}

function handleEdit(row: AchievementRow): void {
  if (!isTypeEditable.value) return
  isEdit.value = true
  if (activeType.value === 'paper') {
    const paperRow = asPaperRow(row)
    Object.assign(paperForm, {
      id: paperRow.id ?? null,
      paperTitle: paperRow.title || paperRow.paperTitle || '',
      authors: paperRow.authors || '',
      authorRank: paperRow.authorRank ?? AUTHOR_RANK.FIRST,
      journalName: paperRow.journalName || '',
      journalLevel: paperRow.journalLevel ?? null,
      impactFactor: paperRow.impactFactor ?? null,
      publicationDate: paperRow.publicationDate || ''
    })
  } else if (activeType.value === 'patent') {
    const patentRow = asPatentRow(row)
    Object.assign(patentForm, {
      id: patentRow.id ?? null,
      patentName: patentRow.patentName || '',
      patentNo: patentRow.patentNo || '',
      patentType: patentRow.patentType || 1,
      applicant: patentRow.applicant || '',
      inventors: patentRow.inventors || '',
      inventorRank: patentRow.inventorRank ?? 1,
      applicantRank: patentRow.applicantRank ?? 1,
      applicationDate: patentRow.applicationDate || '',
      patentStatus: patentRow.patentStatus || 1,
      remark: patentRow.remark || ''
    })
  } else if (activeType.value === 'project') {
    const projectRow = asProjectRow(row)
    Object.assign(projectForm, {
      id: projectRow.id ?? null,
      projectName: projectRow.projectName || '',
      projectType: projectRow.projectType || 1,
      projectLevel: projectRow.projectLevel ?? 1,
      projectNo: projectRow.projectNo || '',
      projectSource: projectRow.projectSource || '',
      leaderName: projectRow.leaderName || '',
      memberRank: projectRow.memberRank ?? 1,
      projectRole: projectRow.projectRole ?? 1,
      participants: projectRow.participants || '',
      startDate: projectRow.startDate || '',
      endDate: projectRow.endDate || '',
      funding: projectRow.funding ?? null,
      projectStatus: projectRow.projectStatus ?? 1,
      remark: projectRow.remark || ''
    })
  } else {
    const competitionRow = asCompetitionRow(row)
    Object.assign(competitionForm, {
      id: competitionRow.id ?? null,
      competitionName: competitionRow.competitionName || '',
      competitionLevel: competitionRow.competitionLevel ?? 1,
      awardLevel: competitionRow.awardLevel ?? 2,
      awardRank: competitionRow.awardRank ?? 1,
      awardType: competitionRow.awardType ?? 1,
      memberRank: competitionRow.memberRank ?? 1,
      instructor: competitionRow.instructor || '',
      issuingUnit: competitionRow.issuingUnit || '',
      organizer: competitionRow.organizer || '',
      teamMembers: competitionRow.teamMembers || '',
      awardDate: competitionRow.awardDate || '',
      remark: competitionRow.remark || ''
    })
  }
  formRef.value?.clearValidate()
  dialogVisible.value = true
  syncRouteQuery()
}

function handleDelete(row: AchievementRow): void {
  ElMessageBox.confirm('确定要删除该论文吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deletePaper(asPaperRow(row).id || 0)
      ElMessage.success('删除成功')
      await fetchTableData()
    })
    .catch(() => undefined)
}

async function handleSubmit(): Promise<void> {
  if (!isTypeEditable.value) return
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (activeType.value === 'paper') {
      const payload = {
        studentId: 0,
        title: paperForm.paperTitle,
        paperTitle: paperForm.paperTitle,
        authors: paperForm.authors,
        journalName: paperForm.journalName,
        journal: paperForm.journalName,
        authorRank: paperForm.authorRank,
        journalLevel: paperForm.journalLevel ?? undefined,
        impactFactor: paperForm.impactFactor ?? undefined,
        publicationDate: paperForm.publicationDate,
        publishDate: paperForm.publicationDate
      }
      if (isEdit.value && paperForm.id) {
        await updatePaper(paperForm.id, payload)
      } else {
        await submitPaper(payload)
      }
    } else if (activeType.value === 'patent') {
      const payload = {
        patentName: patentForm.patentName,
        patentNo: patentForm.patentNo,
        patentType: patentForm.patentType,
        applicant: patentForm.applicant,
        inventors: patentForm.inventors,
        inventorRank: patentForm.inventorRank ?? undefined,
        applicantRank: patentForm.applicantRank ?? undefined,
        applicationDate: patentForm.applicationDate || undefined,
        patentStatus: patentForm.patentStatus,
        remark: patentForm.remark
      }
      if (isEdit.value && patentForm.id) {
        await updatePatent({ id: patentForm.id, ...payload })
      } else {
        await addPatent(payload as Omit<ResearchPatent, 'id'>)
      }
    } else if (activeType.value === 'project') {
      const payload = {
        studentId: 0,
        projectName: projectForm.projectName,
        projectType: projectForm.projectType,
        projectLevel: projectForm.projectLevel ?? undefined,
        projectNo: projectForm.projectNo || undefined,
        projectSource: projectForm.projectSource || undefined,
        leaderName: projectForm.leaderName || undefined,
        memberRank: projectForm.memberRank ?? undefined,
        projectRole: projectForm.projectRole ?? undefined,
        participants: projectForm.participants || undefined,
        startDate: projectForm.startDate || undefined,
        endDate: projectForm.endDate || undefined,
        funding: projectForm.funding ?? undefined,
        projectStatus: projectForm.projectStatus ?? undefined,
        remark: projectForm.remark || undefined
      }
      if (isEdit.value && projectForm.id) {
        await updateProject({ id: projectForm.id, ...payload })
      } else {
        await addProject(payload as Omit<ResearchProject, 'id'>)
      }
    } else {
      const payload = {
        studentId: 0,
        competitionName: competitionForm.competitionName,
        competitionLevel: competitionForm.competitionLevel ?? undefined,
        awardLevel: competitionForm.awardLevel ?? undefined,
        awardRank: competitionForm.awardRank ?? undefined,
        awardType: competitionForm.awardType ?? undefined,
        memberRank: competitionForm.memberRank ?? undefined,
        instructor: competitionForm.instructor || undefined,
        issuingUnit: competitionForm.issuingUnit || undefined,
        organizer: competitionForm.organizer || undefined,
        teamMembers: competitionForm.teamMembers || undefined,
        awardDate: competitionForm.awardDate || undefined,
        remark: competitionForm.remark || undefined
      }
      if (isEdit.value && competitionForm.id) {
        await updateCompetition({ id: competitionForm.id, ...payload })
      } else {
        await addCompetition(payload as Omit<CompetitionAward, 'id'>)
      }
    }

    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
    dialogVisible.value = false
    syncRouteQuery()
    await fetchTableData()
  } catch (error) {
    console.error('提交科研成果失败:', error)
  }
}

function handleDialogClose(): void {
  formRef.value?.resetFields()
  resetCurrentForm()
  syncRouteQuery()
}

function getRouteType(): AchievementType {
  const type = Array.isArray(route.query.type) ? route.query.type[0] : route.query.type
  return achievementTypeOptions.some(item => item.value === type) ? (type as AchievementType) : 'paper'
}

watch(
  () => route.query.type,
  () => {
    const routeType = getRouteType()
    if (routeType !== activeType.value) {
      activeType.value = routeType
      queryParams.current = 1
      queryParams.status = undefined
      fetchTableData()
    }
  }
)

onMounted(async () => {
  activeType.value = getRouteType()
  await fetchTableData()
  const action = Array.isArray(route.query.action) ? route.query.action[0] : route.query.action
  if (action === 'add') {
    handleAdd()
  } else {
    syncRouteQuery()
  }
})
</script>

<style scoped lang="scss">
.achievements-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;

  .page-title {
    margin: 0 0 6px;
    color: #303133;
    font-size: 22px;
    font-weight: 600;
  }

  .page-subtitle {
    margin: 0;
    color: #909399;
    font-size: 13px;
  }
}

.search-form {
  margin: 8px 0 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
