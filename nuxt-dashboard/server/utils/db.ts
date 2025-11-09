import { Pool, PoolClient, QueryResult } from 'pg'

let pool: Pool | null = null

export function getDatabasePool(): Pool {
  const config = useRuntimeConfig()
  
  if (!pool) {
    pool = new Pool({
      host: config.databaseHost,
      port: config.databasePort,
      database: config.databaseName,
      user: config.databaseUser,
      password: config.databasePassword,
      max: 20, // Maximum number of clients in the pool
      idleTimeoutMillis: 30000,
      connectionTimeoutMillis: 2000,
    })

    // Handle pool errors
    pool.on('error', (err) => {
      console.error('Unexpected error on idle client', err)
    })
  }

  return pool
}

export async function query<T = any>(
  text: string,
  params?: any[]
): Promise<QueryResult<T>> {
  const pool = getDatabasePool()
  const start = Date.now()
  
  try {
    const res = await pool.query<T>(text, params)
    const duration = Date.now() - start
    console.log('Executed query', { text, duration, rows: res.rowCount })
    return res
  } catch (error) {
    console.error('Database query error', { text, error })
    throw error
  }
}

export async function getClient(): Promise<PoolClient> {
  const pool = getDatabasePool()
  return await pool.connect()
}

export async function transaction<T>(
  callback: (client: PoolClient) => Promise<T>
): Promise<T> {
  const client = await getClient()
  
  try {
    await client.query('BEGIN')
    const result = await callback(client)
    await client.query('COMMIT')
    return result
  } catch (error) {
    await client.query('ROLLBACK')
    throw error
  } finally {
    client.release()
  }
}

export async function closePool(): Promise<void> {
  if (pool) {
    await pool.end()
    pool = null
  }
}

// Set search path to app schema for all queries
export function setAppSchema(client?: PoolClient) {
  if (client) {
    return client.query('SET search_path TO app, public')
  }
  return query('SET search_path TO app, public')
}

