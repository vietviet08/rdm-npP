import { query } from '~/server/utils/db'

export default defineEventHandler(async (event) => {
  try {
    // Test database connection
    await query('SELECT 1')
    
    return {
      status: 'ok',
      timestamp: new Date().toISOString(),
      services: {
        database: 'connected',
        guacamole: 'configured'
      }
    }
  } catch (error) {
    throw createError({
      statusCode: 500,
      statusMessage: 'Health check failed',
      data: {
        error: error instanceof Error ? error.message : 'Unknown error'
      }
    })
  }
})

