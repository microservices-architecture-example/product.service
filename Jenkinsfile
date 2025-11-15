pipeline {
    agent any
    environment {
        SERVICE = 'product'   
        NAME = "luigilopesz/${env.SERVICE}"  
        AWS_REGION  = 'sa-east-1'
        EKS_CLUSTER = 'eks-store'
    }
    stages {
        stage('Dependecies') {
            steps {
                // dispara o job da interface correspondente e aguarda concluir
                build job: "${env.SERVICE}", wait: true
            }
        }
        stage('Build') { 
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }      
        stage('Build & Push Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credential',
                    usernameVariable: 'USERNAME',
                    passwordVariable: 'TOKEN')]) {
                    sh "docker login -u $USERNAME -p $TOKEN"

                    // builder multi-arch efêmero
                    sh "docker buildx create --use --platform=linux/arm64,linux/amd64 --driver-opt network=host --node multi-platform-builder-${env.SERVICE} --name multi-platform-builder-${env.SERVICE}"

                    // build + push tags :latest e :BUILD_ID
                    sh "docker buildx build --platform=linux/arm64,linux/amd64 --push --tag ${env.NAME}:latest --tag ${env.NAME}:${env.BUILD_ID} -f Dockerfile ."

                    // limpeza do builder
                    sh "docker buildx rm --force multi-platform-builder-${env.SERVICE}"
                }
            }
        }
        stage('Deploy to EKS') {
            agent {
                // Use a docker agent with AWS CLI. Kubectl will be installed in the steps.
                docker {
                    image 'amazon/aws-cli:latest'
                    args '--entrypoint=""'
                }
            }
            steps {
                // Usa credenciais AWS do Jenkins (Access Key / Secret)
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                credentialsId: 'aws-credential',
                accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                sh """
                    # Instala o kubectl, necessário para os comandos seguintes
                    curl -LO "https://dl.k8s.io/release/\$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
                    chmod +x ./kubectl
                    mv ./kubectl /usr/local/bin/

                    # garante diretório padrão do kubeconfig
                    mkdir -p ~/.kube

                    # configura contexto do cluster no caminho padrão (~/.kube/config)
                    aws eks update-kubeconfig --region ${AWS_REGION} --name ${EKS_CLUSTER}

                    kubectl config current-context

                    # Aplica as configurações do k8s.
                    # Isso vai criar o deployment se não existir, ou atualizá-lo se já existir.
                    kubectl apply -f ./k8s/

                    # atualiza a imagem do Deployment
                    kubectl set image deploy/${SERVICE} ${SERVICE}=${NAME}:${BUILD_ID} --record

                    # espera o rollout
                    kubectl rollout status deployment/${SERVICE} --timeout=180s
                """
                }
            }
        }
    }
}