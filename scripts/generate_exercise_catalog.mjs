import { mkdir, writeFile } from 'node:fs/promises'
import { pathToFileURL } from 'node:url'
import path from 'node:path'

const [sourceRoot, outputFile] = process.argv.slice(2)

if (!sourceRoot || !outputFile) {
  throw new Error('Usage: node generate_exercise_catalog.mjs <source-root> <output-file>')
}

const sources = [
  ['chestExercises.js', 'CHEST', 'chest'],
  ['backExercises.js', 'BACK', 'back'],
  ['shoulderExercises.js', 'SHOULDERS', 'shoulders'],
  ['bicepsExercises.js', 'BICEPS', 'biceps'],
  ['tricepsExercises.js', 'TRICEPS', 'triceps'],
  ['legExercises.js', 'QUADRICEPS', 'legs'],
  ['coreExercises.js', 'CORE', 'core'],
]

const existingExerciseIds = new Set([
  'bench-press-barbell',
  'barbell-squat',
  'deadlift',
  'barbell-bent-over-row',
  'barbell-shoulder-press',
])

const exercisesWithoutMedia = new Set(['hip-thrust', 'plank', 'side-plank'])

const groupAliases = new Map([
  ['peito', 'CHEST'],
  ['costas', 'BACK'],
  ['ombros', 'SHOULDERS'],
  ['quadríceps', 'QUADRICEPS'],
  ['posteriores', 'HAMSTRINGS'],
  ['posterior de coxa', 'HAMSTRINGS'],
  ['glúteos', 'GLUTES'],
  ['bíceps', 'BICEPS'],
  ['tríceps', 'TRICEPS'],
  ['panturrilhas', 'CALVES'],
  ['abdômen', 'CORE'],
  ['core', 'CORE'],
  ['lombar', 'BACK'],
])

function normalize(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
}

function muscleGroup(value, fallback) {
  const normalized = normalize(value)
  for (const [label, group] of groupAliases) {
    if (normalize(label) === normalized) return group
  }
  return fallback
}

function equipment(value) {
  const normalized = normalize(value)
  if (normalized.includes('barra')) return 'BARBELL'
  if (normalized.includes('halter')) return 'DUMBBELL'
  if (normalized.includes('maquina')) return 'MACHINE'
  if (normalized.includes('cabo') || normalized.includes('polia')) return 'CABLE'
  if (normalized.includes('corporal') || normalized.includes('peso do corpo')) {
    return 'BODYWEIGHT'
  }
  return 'OTHER'
}

const catalog = []

for (const [fileName, fallbackGroup, mediaFolder] of sources) {
  const moduleUrl = pathToFileURL(path.join(sourceRoot, fileName)).href
  const module = await import(moduleUrl)
  const exercises = Object.values(module).find(Array.isArray) || []

  for (const exercise of exercises) {
    if (existingExerciseIds.has(exercise.id)) continue

    catalog.push({
      id: `forgeflow-catalog-${exercise.id}`,
      name: exercise.name,
      primaryMuscleGroup: muscleGroup(exercise.muscleGroup, fallbackGroup),
      secondaryMuscleGroups: (exercise.secondaryMuscles || [])
        .map((value) => muscleGroup(value, null))
        .filter(Boolean),
      equipment: equipment(exercise.equipment),
      instructions: (exercise.instructions || []).join('\n'),
      mediaUri: exercisesWithoutMedia.has(exercise.id)
        ? null
        : `file:///android_asset/exercise-media/${mediaFolder}/${exercise.id}.gif`,
    })
  }
}

await mkdir(path.dirname(outputFile), { recursive: true })
await writeFile(outputFile, `${JSON.stringify(catalog, null, 2)}\n`, 'utf8')
console.log(`Generated ${catalog.length} native exercise definitions.`)
