/**
 * ESLint 配置文件
 * 支持 Vue 3 + TypeScript + Prettier
 */

import globals from 'globals'
import pluginVue from 'eslint-plugin-vue'
import pluginTs from '@typescript-eslint/eslint-plugin'
import parserTs from '@typescript-eslint/parser'
import configPrettier from 'eslint-config-prettier'

export default [
  // 基础配置
  {
    name: 'app/base',
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
        ...globals.es2021
      },
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module'
      }
    },
    rules: {
      // 推荐规则
      'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
      'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',

      // 代码风格
      'no-unused-vars': ['error', {
        vars: 'all',
        args: 'after-used',
        caughtErrors: 'none',
        varsIgnorePattern: '^_|^h$|^defineProps$|^defineEmits$|^defineSlots$|^defineOptions$|^defineExpose$'
      }],
      'prefer-const': 'error',
      'no-var': 'error',
      'no-let': 'off',

      // 最佳实践
      'eqeqeq': ['error', 'always', { null: 'ignore' }],
      'curly': ['error', 'multi-line'],
      'no-eval': 'error',
      'no-implied-eval': 'error',

      // 可读性
      'semi': ['error', 'never'],
      'quotes': ['error', 'single', { avoidEscape: true }],
      'indent': 'off',
      'comma-dangle': ['error', 'never'],
      'brace-style': ['error', '1tbs', { allowSingleLine: true }],
      'object-curly-spacing': ['error', 'always'],
      'array-bracket-spacing': ['error', 'never'],
      'space-before-function-paren': ['error', 'always'],
      'space-in-parens': ['error', 'never'],
      'computed-property-spacing': ['error', 'never'],
      'no-multiple-empty-lines': ['error', { max: 1, maxEOF: 0 }],
      'eol-last': ['error', 'always']
    }
  },

  // Vue 文件配置
  ...pluginVue.configs['flat/recommended'],
  {
    name: 'app/vue',
    files: ['**/*.vue', '**/*.vue.js'],
    languageOptions: {
      parserOptions: {
        parser: parserTs
      }
    },
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/no-mutating-props': 'off',
      'vue/no-setup-props-destructure': 'off',
      'vue/require-default-prop': 'off',
      'vue/no-v-html': 'off',
      'vue/custom-event-name-casing': ['warn', 'kebab-case'],
      'vue/attributes-order': 'warn',
      'vue/html-self-closing': ['error', {
        html: {
          void: 'always',
          normal: 'always',
          component: 'always'
        },
        svg: 'always',
        math: 'always'
      }]
    }
  },

  // TypeScript 配置
  {
    name: 'app/typescript',
    files: ['**/*.ts', '**/*.tsx', '**/*.vue'],
    languageOptions: {
      parser: parserTs,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        project: ['./tsconfig.json'],
        extraFileExtensions: ['.vue']
      }
    },
    plugins: {
      '@typescript-eslint': pluginTs
    },
    rules: {
      ...pluginTs.configs.recommended.rules,
      ...pluginTs.configs['recommended-requiring-type-checking'].rules,

      // 自定义 TypeScript 规则
      '@typescript-eslint/no-unused-vars': ['error', {
        vars: 'all',
        args: 'after-used',
        caughtErrors: 'none',
        varsIgnorePattern: '^_|^h$|^DefineProps$|^DefineEmits$|^DefineSlots$|^DefineOptions$|^DefineExpose$'
      }],
      '@typescript-eslint/explicit-module-boundary-types': 'off',
      '@typescript-eslint/no-explicit-any': 'warn',
      '@typescript-eslint/no-unsafe-assignment': 'warn',
      '@typescript-eslint/no-unsafe-member-access': 'warn',
      '@typescript-eslint/no-unsafe-call': 'warn',
      '@typescript-eslint/no-unsafe-return': 'warn',
      '@typescript-eslint/restrict-template-expressions': ['warn', { allowString: true, allowNumber: true }],
      '@typescript-eslint/no-floating-promises': 'warn',
      '@typescript-eslint/prefer-nullish-coalescing': 'warn',
      '@typescript-eslint/prefer-optional-chain': 'warn',
      '@typescript-eslint/ban-ts-comment': ['warn', { 'ts-ignore': 'allow-with-description' }]
    }
  },

  // Prettier 配置（必须放在最后）
  configPrettier
]
