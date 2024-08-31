<script>
    const technologies = [
        'Java 17',
        'PostgreSQL 15',
        'svelte@4.2.18'
    ];

    const sqlSchema = `
-- Clients
CREATE TABLE clients (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL
);

-- Admins
CREATE TABLE admins (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    confirmation_code INT,
    confirmation_code_expiry TIMESTAMP,
    is_authorized BOOLEAN NOT NULL DEFAULT FALSE
);

-- Servicemen
CREATE TABLE servicemen (
    serviceman_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL
);

-- EHelpdesks
CREATE TABLE ehelpdesks (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Incidents
CREATE TABLE incidents (
    incident_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    hash VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    incident_number VARCHAR(255) NOT NULL,
    ehelpdesk_id BIGINT NOT NULL,
    incident_topic VARCHAR(255) NOT NULL,
    serviceman_id BIGINT NOT NULL,
    is_done BOOLEAN NOT NULL,
    question_version INT NOT NULL,
    CONSTRAINT fk_incident_user FOREIGN KEY (user_id) REFERENCES clients(id),
    CONSTRAINT fk_incident_ehelpdesk FOREIGN KEY (ehelpdesk_id) REFERENCES ehelpdesks(id),
    CONSTRAINT fk_incident_serviceman FOREIGN KEY (serviceman_id) REFERENCES servicemen(serviceman_id),
    UNIQUE (incident_number, ehelpdesk_id)
);

-- Questions
CREATE TABLE questions (
    question_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    question_text TEXT NOT NULL,
    question_type VARCHAR(255) NOT NULL CHECK (question_type IN ('STARS', 'RADIO')),
    version INT NOT NULL
);

-- Answers
CREATE TABLE answers (
    answer_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    incident_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_value VARCHAR(255) NOT NULL,
    answer_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_answer_incident FOREIGN KEY (incident_id) REFERENCES incidents(incident_id),
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES questions(question_id)
);
`;

    const apiEndpoint = {
        url: '{store.js -> backendBaseUrl}/api/form/sendform',
        method: 'POST',
        jsonFormat: {
            email: '',
            clientFirstName: '',
            servicemanFirstName: '',
            servicemanLastName: '',
            incidentNumber: '',
            incidentTopic: '',
            ehelpdeskId: '',
            questionVersion: ''
        }
    };
</script>

<main>
    <h1>Info</h1>

    <section>
        <h2>Technologie używane w projekcie</h2>
        <ul>
            {#each technologies as tech}
                <li>{tech}</li>
            {/each}
        </ul>
    </section>

    <section>
        <h2>Endpoint API</h2>
        <p>Adres: <code>{apiEndpoint.url}</code></p>
        <p>Metoda: <code>{apiEndpoint.method}</code></p>
        <p>Format:</p>
        <pre>{JSON.stringify(apiEndpoint.jsonFormat, null, 2)}</pre>
    </section>

    <section>
        <h2>Autor</h2>
        <ul>
            <li>Mateusz Musiał</li>
            <li>matimusial5@gmail.com</li>
            <li>+48 600 551 892</li>
        </ul>
    </section>

    <section>
        <h2>Schemat Bazy Danych</h2>
        <svg viewBox="0 0 800 500">
            <!-- Tabela Clients -->
            <rect x="20" y="20" width="150" height="100" fill="#f0f0f0" stroke="#005177" stroke-width="2"/>
            <text x="30" y="40" font-family="Arial" font-size="14" fill="#005177">Clients</text>
            <text x="30" y="60" font-family="Arial" font-size="12">id (PK)</text>
            <text x="30" y="80" font-family="Arial" font-size="12">email</text>
            <text x="30" y="100" font-family="Arial" font-size="12">first_name</text>

            <!-- Tabela Admins -->
            <rect x="200" y="20" width="200" height="180" fill="#f0f0f0" stroke="#005177" stroke-width="2"/>
            <text x="210" y="40" font-family="Arial" font-size="14" fill="#005177">Admins</text>
            <text x="210" y="60" font-family="Arial" font-size="12">id (PK)</text>
            <text x="210" y="80" font-family="Arial" font-size="12">username</text>
            <text x="210" y="100" font-family="Arial" font-size="12">email</text>
            <text x="210" y="120" font-family="Arial" font-size="12">password</text>
            <text x="210" y="140" font-family="Arial" font-size="12">confirmation_code</text>
            <text x="210" y="160" font-family="Arial" font-size="12">confirmation_code_expiry</text>
            <text x="210" y="180" font-family="Arial" font-size="12">is_authorized</text>

            <!-- Tabela Servicemen -->
            <rect x="420" y="20" width="180" height="100" fill="#f0f0f0" stroke="#005177" stroke-width="2"/>
            <text x="430" y="40" font-family="Arial" font-size="14" fill="#005177">Servicemen</text>
            <text x="430" y="60" font-family="Arial" font-size="12">serviceman_id (PK)</text>
            <text x="430" y="80" font-family="Arial" font-size="12">first_name</text>
            <text x="430" y="100" font-family="Arial" font-size="12">last_name</text>

            <!-- Tabela EHelpdesks -->
            <rect x="640" y="20" width="150" height="80" fill="#f0f0f0" stroke="#005177" stroke-width="2"/>
            <text x="650" y="40" font-family="Arial" font-size="14" fill="#005177">EHelpdesks</text>
            <text x="650" y="60" font-family="Arial" font-size="12">id (PK)</text>
            <text x="650" y="80" font-family="Arial" font-size="12">name</text>

            <!-- Tabela Incidents -->
            <rect x="20" y="220" width="280" height="220" fill="#f0f0f0" stroke="#005177" stroke-width="2"/>
            <text x="30" y="240" font-family="Arial" font-size="14" fill="#005177">Incidents</text>
            <text x="30" y="260" font-family="Arial" font-size="12">incident_id (PK)</text>
            <text x="30" y="280" font-family="Arial" font-size="12">hash</text>
            <text x="30" y="300" font-family="Arial" font-size="12">user_id (FK)</text>
            <text x="30" y="320" font-family="Arial" font-size="12">incident_number</text>
            <text x="30" y="340" font-family="Arial" font-size="12">ehelpdesk_id (FK)</text>
            <text x="30" y="360" font-family="Arial" font-size="12">incident_topic</text>
            <text x="30" y="380" font-family="Arial" font-size="12">serviceman_id (FK)</text>
            <text x="30" y="400" font-family="Arial" font-size="12">is_done</text>
            <text x="30" y="420" font-family="Arial" font-size="12">question_version</text>

            <!-- Tabela Questions -->
            <rect x="330" y="220" width="200" height="120" fill="#f0f0f0" stroke="#005177" stroke-width="2"/>
            <text x="340" y="240" font-family="Arial" font-size="14" fill="#005177">Questions</text>
            <text x="340" y="260" font-family="Arial" font-size="12">question_id (PK)</text>
            <text x="340" y="280" font-family="Arial" font-size="12">question_text</text>
            <text x="340" y="300" font-family="Arial" font-size="12">question_type</text>
            <text x="340" y="320" font-family="Arial" font-size="12">version</text>

            <!-- Tabela Answers -->
            <rect x="550" y="220" width="200" height="140" fill="#f0f0f0" stroke="#005177" stroke-width="2"/>
            <text x="560" y="240" font-family="Arial" font-size="14" fill="#005177">Answers</text>
            <text x="560" y="260" font-family="Arial" font-size="12">answer_id (PK)</text>
            <text x="560" y="280" font-family="Arial" font-size="12">incident_id (FK)</text>
            <text x="560" y="300" font-family="Arial" font-size="12">question_id (FK)</text>
            <text x="560" y="320" font-family="Arial" font-size="12">answer_value</text>
            <text x="560" y="340" font-family="Arial" font-size="12">answer_date</text>
        </svg>
    </section>

    <section>
        <h2>SQL</h2>
        <pre>{sqlSchema}</pre>
    </section>
</main>

<style>
    svg {
        max-width: 800px;
        margin: 20px auto;
    }

    section {
        margin-bottom: 2em;
    }
</style>
