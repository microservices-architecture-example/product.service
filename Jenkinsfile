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
                    sh "docker buildx create --use --platform=linux/arm64,linux/amd64 --node multi-platform-builder-${env.SERVICE} --name multi-platform-builder-${env.SERVICE}"

                    // build + push tags :latest e :BUILD_ID
                    sh "docker buildx build --platform=linux/arm64,linux/amd64 --push --tag ${env.NAME}:latest --tag ${env.NAME}:${env.BUILD_ID} -f Dockerfile ."

                    // limpeza do builder
                    sh "docker buildx rm --force multi-platform-builder-${env.SERVICE}"
                }
            }
        }
        stage('Deploy to EKS') {
            steps {
                // Usa credenciais AWS do Jenkins (Access Key / Secret)
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                credentialsId: 'aws-credential',
                accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                sh """
                    # garante diretório padrão do kubeconfig
                    mkdir -p ~/.kube

                    # configura contexto do cluster no caminho padrão (~/.kube/config)
                    aws eks update-kubeconfig --region ${AWS_REGION} --name ${EKS_CLUSTER}

                    kubectl config current-context

                    # aplica manifest inicial se ainda não existir
                    if ! kubectl get deploy ${SERVICE} >/dev/null 2>&1; then
                    kubectl apply -f ./k8s/
                    fi

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