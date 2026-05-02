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
        <el-button type="primary" @click="handleSearch">查询</el-button>
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
      @current-change="handlePageChange"
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
                v-for="item in AUTHOR_RANK_OPTIONS"
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
                v-for="item in JOURNAL_LEVEL_OPTIONS"
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
                v-for="item in PATENT_TYPE_OPTIONS"
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
                v-for="item in PATENT_STATUS_OPTIONS"
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
                v-for="item in PROJECT_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="项目级别" prop="projectLevel">
            <el-select v-model="projectForm.projectLevel" placeholder="请选择">
              <el-option
                v-for="item in PROJECT_LEVEL_OPTIONS"
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
                v-for="item in PROJECT_ROLE_OPTIONS"
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
                v-for="item in PROJECT_STATUS_OPTIONS"
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
                v-for="item in COMPETITION_LEVEL_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="获奖等级" prop="awardLevel">
            <el-select v-model="competitionForm.awardLevel" placeholder="请选择">
              <el-option
                v-for="item in COMPETITION_AWARD_LEVEL_OPTIONS"
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
                v-for="item in COMPETITION_AWARD_TYPE_OPTIONS"
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
        <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" :title="`${activeTypeLabel}详情`" width="640px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item
          v-for="(field, idx) in detailFields"
          :key="idx"
          :label="field.label"
          :span="field.span ?? 1"
        >{{ field.value(currentRow) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
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
  getProjectPage,
  type ProjectPageParams,
  type ResearchProject
} from '@/api/project'
import {
  getCompetitionPage,
  type CompetitionAward,
  type CompetitionPageParams
} from '@/api/competition'
import {
  APPLICATION_AUDIT_STATUS,
  APPLICATION_AUDIT_STATUS_LABELS,
  APPLICATION_AUDIT_STATUS_TYPES,
  extractPageData,
  isRequestCanceled
} from '@/utils/helpers'
import {
  ACHIEVEMENT_TYPE_OPTIONS,
  AUTHOR_RANK,
  AUTHOR_RANK_OPTIONS,
  COMPETITION_AWARD_LEVEL_OPTIONS,
  COMPETITION_AWARD_TYPE_OPTIONS,
  COMPETITION_LEVEL_OPTIONS,
  getAchievementOptionLabel,
  JOURNAL_LEVEL_OPTIONS,
  PROJECT_ROLE_OPTIONS,
  PROJECT_LEVEL_OPTIONS,
  PROJECT_STATUS_OPTIONS,
  PROJECT_TYPE_OPTIONS
} from '@/constants/achievement'
import {
  GENERIC_AUDIT_LABELS as SHARED_GENERIC_AUDIT_LABELS,
  GENERIC_AUDIT_OPTIONS as SHARED_GENERIC_AUDIT_OPTIONS,
  GENERIC_AUDIT_TYPES as SHARED_GENERIC_AUDIT_TYPES
} from '@/constants/achievementReview'
import { PATENT_STATUS_OPTIONS, PATENT_TYPE_OPTIONS } from '@/constants/patent'

defineOptions({ name: 'StudentAchievements' })

type AchievementType = 'paper' | 'patent' | 'project' | 'competition'
type NormalizedPaper = Paper & {
  title: string
  journalName: string
  publicationDate: string
}
type AuditedAchievementRow = ResearchPatent | ResearchProject | CompetitionAward
type AchievementRow = NormalizedPaper | AuditedAchievementRow

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

const achievementTypeOptions = [...ACHIEVEMENT_TYPE_OPTIONS] as Array<{ label: string; value: AchievementType }>


const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const sizeChangePending = ref(false)
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
        { label: APPLICATION_AUDIT_STATUS_LABELS[APPLICATION_AUDIT_STATUS.PENDING], value: APPLICATION_AUDIT_STATUS.PENDING },
        { label: APPLICATION_AUDIT_STATUS_LABELS[APPLICATION_AUDIT_STATUS.TUTOR_APPROVED], value: APPLICATION_AUDIT_STATUS.TUTOR_APPROVED },
        { label: APPLICATION_AUDIT_STATUS_LABELS[APPLICATION_AUDIT_STATUS.DEPARTMENT_APPROVED], value: APPLICATION_AUDIT_STATUS.DEPARTMENT_APPROVED },
        { label: APPLICATION_AUDIT_STATUS_LABELS[APPLICATION_AUDIT_STATUS.REJECTED], value: APPLICATION_AUDIT_STATUS.REJECTED }
      ]
    : SHARED_GENERIC_AUDIT_OPTIONS
)

const currentFormModel = computed<Record<string, any>>(() =>
  activeType.value === 'patent' ? patentForm : paperForm
)

const currentFormRules = computed<FormRules>(() =>
  activeType.value === 'patent' ? (patentRules as FormRules) : (paperRules as FormRules)
)

const dialogTitle = computed(() => `${isEdit.value ? '编辑' : '添加'}${activeTypeLabel.value}`)
function normalizePaper(row: Paper): NormalizedPaper {
  return {
    ...row,
    title: row.title || row.paperTitle || '',
    journalName: row.journalName || row.journal || '',
    publicationDate: row.publicationDate || row.publishDate || row.date || ''
  }
}

function isPaperRow(row: AchievementRow): row is NormalizedPaper {
  return 'title' in row && typeof (row as NormalizedPaper).title === 'string'
}

function isPatentRow(row: AchievementRow): row is ResearchPatent {
  return 'patentName' in row && typeof (row as ResearchPatent).patentName === 'string'
}

function isProjectRow(row: AchievementRow): row is ResearchProject {
  return 'projectName' in row && typeof (row as ResearchProject).projectName === 'string'
}

function isCompetitionRow(row: AchievementRow): row is CompetitionAward {
  return 'competitionName' in row && typeof (row as CompetitionAward).competitionName === 'string'
}

function getAuthorRankLabel(value?: number): string {
  return getAchievementOptionLabel(AUTHOR_RANK_OPTIONS, value)
}

function getJournalLevelLabel(value?: number): string {
  return getAchievementOptionLabel(JOURNAL_LEVEL_OPTIONS, value)
}

function getPatentTypeLabel(value?: number): string {
  return getAchievementOptionLabel(PATENT_TYPE_OPTIONS, value)
}

function getPatentStatusLabel(value?: number): string {
  return getAchievementOptionLabel(PATENT_STATUS_OPTIONS, value)
}

function getProjectTypeLabel(value?: number): string {
  return getAchievementOptionLabel(PROJECT_TYPE_OPTIONS, value)
}

function getProjectLevelLabel(value?: number): string {
  return getAchievementOptionLabel(PROJECT_LEVEL_OPTIONS, value)
}

function getProjectRoleLabel(value?: number): string {
  return getAchievementOptionLabel(PROJECT_ROLE_OPTIONS, value)
}

function getProjectStatusLabel(value?: number): string {
  return getAchievementOptionLabel(PROJECT_STATUS_OPTIONS, value)
}

function getCompetitionLevelLabel(value?: number): string {
  return getAchievementOptionLabel(COMPETITION_LEVEL_OPTIONS, value)
}

function getCompetitionAwardLevelLabel(value?: number): string {
  return getAchievementOptionLabel(COMPETITION_AWARD_LEVEL_OPTIONS, value)
}

function getCompetitionAwardTypeLabel(value?: number): string {
  return getAchievementOptionLabel(COMPETITION_AWARD_TYPE_OPTIONS, value)
}

function getRowStatusValue(row: AchievementRow): number | undefined {
  return isPaperRow(row) ? row.status : row.auditStatus
}

function getRowStatusLabel(row: AchievementRow): string {
  const status = getRowStatusValue(row)
  if (status === undefined || status === null) return '-'
  return activeType.value === 'paper'
    ? APPLICATION_AUDIT_STATUS_LABELS[status] || '未知状态'
    : SHARED_GENERIC_AUDIT_LABELS[status] || '未知状态'
}

function getRowStatusType(row: AchievementRow): 'warning' | 'success' | 'danger' | 'info' | 'primary' {
  const status = getRowStatusValue(row)
  if (status === undefined || status === null) return 'info'
  return activeType.value === 'paper'
    ? APPLICATION_AUDIT_STATUS_TYPES[status] || 'info'
    : SHARED_GENERIC_AUDIT_TYPES[status] || 'info'
}

function canEditRow(row: AchievementRow): boolean {
  return isTypeEditable.value && getRowStatusValue(row) === APPLICATION_AUDIT_STATUS.PENDING
}

function canDeleteRow(row: AchievementRow): boolean {
  return isPaperRow(row) && row.status === APPLICATION_AUDIT_STATUS.PENDING
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
    if (isRequestCanceled(error)) return
    ElMessage.error('加载科研成果失败')
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
  void fetchTableData()
}

function handleSearch(): void {
  queryParams.current = 1
  void fetchTableData()
}

function handlePageChange(_page: number): void {
  if (sizeChangePending.value) {
    sizeChangePending.value = false
    return
  }
  void fetchTableData()
}

function handleReset(): void {
  queryParams.status = undefined
  queryParams.current = 1
  void fetchTableData()
}

function handleSizeChange(): void {
  sizeChangePending.value = true
  queryParams.current = 1
  void fetchTableData()
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

interface DetailField {
  label: string
  span?: number
  value: (row: any) => string
}

interface TypeFieldMapping {
  formModel?: Record<string, any>
  extractEditFields?: (row: Record<string, any>) => Record<string, any>
  getFormPayload?: (form: Record<string, any>, isEdit: boolean) => Record<string, any>
  submitCreate?: (payload: any) => Promise<any>
  submitUpdate?: (id: number, payload: any) => Promise<any>
  detailFields: DetailField[]
}

const TYPE_FIELD_CONFIG: Record<string, TypeFieldMapping> = {
  paper: {
    formModel: paperForm,
    extractEditFields: (row) => ({
      id: row.id ?? null,
      paperTitle: row.title || row.paperTitle || '',
      authors: row.authors || '',
      authorRank: row.authorRank ?? AUTHOR_RANK.FIRST,
      journalName: row.journalName || '',
      journalLevel: row.journalLevel ?? null,
      impactFactor: row.impactFactor ?? null,
      publicationDate: row.publicationDate || ''
    }),
    getFormPayload: (form, isEdit) => ({
      title: form.paperTitle,
      paperTitle: form.paperTitle,
      authors: form.authors,
      journalName: form.journalName,
      journal: form.journalName,
      authorRank: form.authorRank,
      journalLevel: form.journalLevel ?? undefined,
      impactFactor: form.impactFactor ?? undefined,
      publicationDate: form.publicationDate,
      publishDate: form.publicationDate
    }),
    submitCreate: submitPaper,
    submitUpdate: (id, payload) => updatePaper(id, payload),
    detailFields: [
      { label: '论文标题', span: 2, value: (row) => row.title || '-' },
      { label: '作者', value: (row) => row.authors || '-' },
      { label: '作者排名', value: (row) => getAuthorRankLabel(row.authorRank) },
      { label: '期刊名称', value: (row) => row.journalName || '-' },
      { label: '期刊级别', value: (row) => getJournalLevelLabel(row.journalLevel) },
      { label: '影响因子', value: (row) => row.impactFactor ?? '-' },
      { label: '发表日期', value: (row) => row.publicationDate || '-' },
      { label: '审核状态', value: (row) => getRowStatusLabel(row) },
      { label: '审核意见', span: 2, value: (row) => row.reviewComment || '-' }
    ]
  },
  patent: {
    formModel: patentForm,
    extractEditFields: (row) => ({
      id: row.id ?? null,
      patentName: row.patentName || '',
      patentNo: row.patentNo || '',
      patentType: row.patentType || 1,
      applicant: row.applicant || '',
      inventors: row.inventors || '',
      inventorRank: row.inventorRank ?? 1,
      applicantRank: row.applicantRank ?? 1,
      applicationDate: row.applicationDate || '',
      patentStatus: row.patentStatus || 1,
      remark: row.remark || ''
    }),
    getFormPayload: (form, _isEdit) => ({
      patentName: form.patentName,
      patentNo: form.patentNo,
      patentType: form.patentType,
      applicant: form.applicant,
      inventors: form.inventors,
      inventorRank: form.inventorRank ?? undefined,
      applicantRank: form.applicantRank ?? undefined,
      applicationDate: form.applicationDate || undefined,
      patentStatus: form.patentStatus,
      remark: form.remark
    }),
    submitCreate: (payload) => addPatent(payload as Omit<ResearchPatent, 'id'>),
    submitUpdate: (id, payload) => updatePatent({ id, ...payload }),
    detailFields: [
      { label: '专利名称', span: 2, value: (row) => row.patentName || '-' },
      { label: '专利号', value: (row) => row.patentNo || '-' },
      { label: '专利类型', value: (row) => getPatentTypeLabel(row.patentType) },
      { label: '申请人', value: (row) => row.applicant || '-' },
      { label: '发明人', value: (row) => row.inventors || '-' },
      { label: '发明人排名', value: (row) => row.inventorRank ?? '-' },
      { label: '申请人排名', value: (row) => row.applicantRank ?? '-' },
      { label: '申请日期', value: (row) => row.applicationDate || '-' },
      { label: '专利状态', value: (row) => getPatentStatusLabel(row.patentStatus) },
      { label: '审核状态', value: (row) => getRowStatusLabel(row) },
      { label: '审核意见', span: 2, value: (row) => row.auditComment || '-' },
      { label: '备注', span: 2, value: (row) => row.remark || '-' }
    ]
  },
  project: {
    detailFields: [
      { label: '项目名称', span: 2, value: (row) => row.projectName || '-' },
      { label: '项目类型', value: (row) => getProjectTypeLabel(row.projectType) },
      { label: '项目级别', value: (row) => getProjectLevelLabel(row.projectLevel) },
      { label: '项目编号', value: (row) => row.projectNo || '-' },
      { label: '项目来源', value: (row) => row.projectSource || '-' },
      { label: '负责人', value: (row) => row.leaderName || '-' },
      { label: '成员排名', value: (row) => row.memberRank ?? '-' },
      { label: '项目角色', value: (row) => getProjectRoleLabel(row.projectRole) },
      { label: '参与人员', span: 2, value: (row) => row.participants || '-' },
      { label: '开始日期', value: (row) => row.startDate || '-' },
      { label: '结束日期', value: (row) => row.endDate || '-' },
      { label: '项目经费', value: (row) => row.funding ?? '-' },
      { label: '项目状态', value: (row) => getProjectStatusLabel(row.projectStatus) },
      { label: '审核状态', value: (row) => getRowStatusLabel(row) },
      { label: '审核意见', span: 2, value: (row) => row.auditComment || '-' },
      { label: '备注', span: 2, value: (row) => row.remark || '-' }
    ]
  },
  competition: {
    detailFields: [
      { label: '竞赛名称', span: 2, value: (row) => row.competitionName || '-' },
      { label: '竞赛级别', value: (row) => getCompetitionLevelLabel(row.competitionLevel) },
      { label: '获奖等级', value: (row) => getCompetitionAwardLevelLabel(row.awardLevel) },
      { label: '获奖名次', value: (row) => row.awardRank ?? '-' },
      { label: '获奖类型', value: (row) => getCompetitionAwardTypeLabel(row.awardType) },
      { label: '成员排名', value: (row) => row.memberRank ?? '-' },
      { label: '指导老师', value: (row) => row.instructor || '-' },
      { label: '颁发单位', value: (row) => row.issuingUnit || '-' },
      { label: '主办单位', value: (row) => row.organizer || '-' },
      { label: '获奖日期', value: (row) => row.awardDate || '-' },
      { label: '团队成员', span: 2, value: (row) => row.teamMembers || '-' },
      { label: '审核状态', value: (row) => getRowStatusLabel(row) },
      { label: '审核意见', value: (row) => row.auditComment || '-' },
      { label: '备注', span: 2, value: (row) => row.remark || '-' }
    ]
  }
}

const detailFields = computed<DetailField[]>(() =>
  TYPE_FIELD_CONFIG[activeType.value]?.detailFields ?? []
)

function handleEdit(row: AchievementRow): void {
  if (!isTypeEditable.value) return
  const config = TYPE_FIELD_CONFIG[activeType.value as 'paper' | 'patent']
  if (!config || !config.formModel || !config.extractEditFields) return
  isEdit.value = true
  Object.assign(config.formModel, config.extractEditFields(row as Record<string, any>))
  formRef.value?.clearValidate()
  dialogVisible.value = true
  syncRouteQuery()
}

function handleDelete(row: AchievementRow): void {
  if (!isPaperRow(row)) return
  ElMessageBox.confirm('确定要删除该论文吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deletePaper(row.id || 0)
      ElMessage.success('删除成功')
      await fetchTableData()
    })
    .catch((error) => {
      if (error !== 'cancel' && error !== 'close') {
        console.error('删除失败:', error)
        ElMessage.error('删除失败，请稍后重试')
      }
    })
}

async function handleSubmit(): Promise<void> {
  if (!isTypeEditable.value) return
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const config = TYPE_FIELD_CONFIG[activeType.value as 'paper' | 'patent']
  if (!config || !config.formModel || !config.getFormPayload || !config.submitCreate || !config.submitUpdate) return

  saving.value = true
  try {
    const payload = config.getFormPayload(config.formModel, isEdit.value)
    const id = config.formModel.id
    if (isEdit.value && id) {
      await config.submitUpdate(id, payload)
    } else {
      await config.submitCreate(payload)
    }

    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
    dialogVisible.value = false
    syncRouteQuery()
    await fetchTableData()
  } catch (error) {
    console.error('提交科研成果失败:', error)
    ElMessage.error('提交科研成果失败，请稍后重试')
  } finally {
    saving.value = false
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

const stopTypeWatcher = watch(
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

onUnmounted(() => {
  stopTypeWatcher()
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
