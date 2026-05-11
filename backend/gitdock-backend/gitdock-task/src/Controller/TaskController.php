<?php

namespace App\Controller;

use App\Client\AuthClient;
use App\Client\ProjectClient;
use App\DTOs\DTOtasks\TaskDto;
use App\Service\TaskService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/api/tasks')]
class TaskController extends AbstractController
{
    public function __construct(
        private readonly TaskService $taskService,
        private readonly AuthClient $authClient,
        private readonly ProjectClient $projectClient,
    ) {}

    // ── GET /api/tasks ──────────────────────────────────────────────
    #[Route('', name: 'task_list', methods: ['GET'])]
    public function list(Request $request): JsonResponse
    {
        $authHeader = $request->headers->get('Authorization', '');
        $token = str_replace('Bearer ', '', $authHeader);
        $user = $this->authClient->validateToken($token);
        if (!$user) {
            return $this->json(['error' => 'Unauthorized'], 401);
        }

        $tasks = $this->taskService->getAll();

        // Filtre optionnel ?assigned_to=ID
        $assignedTo = $request->query->get('assigned_to');
        if ($assignedTo) {
            $tasks = array_values(array_filter($tasks, function ($task) use ($assignedTo) {
                $assigned = $task->getAssignedTo();
                return $assigned && $assigned->getId() == (int) $assignedTo;
            }));
        }

        return $this->json(
            ['success' => true, 'data' => $tasks],
            200, [],
            ['groups' => ['task:read']]
        );
    }

    // ── GET /api/tasks/form-data ────────────────────────────────────
    #[Route('/form-data', name: 'task_form_data', methods: ['GET'])]
    public function formData(Request $request): JsonResponse
    {
        $authHeader = $request->headers->get('Authorization', '');
        $token = str_replace('Bearer ', '', $authHeader);

        $user = $this->authClient->validateToken($token);
        if (!$user) {
            return $this->json(['error' => 'Unauthorized'], 401);
        }

        // ✅ Passe le token à getManagerProjects
        $projects = $this->projectClient->getManagerProjects($token);

        $projectsWithCollaborators = array_map(function ($project) use ($token) {
            $projectId = $project['id'] ?? null;
            $collaborators = $projectId
                ? $this->authClient->getProjectCollaborators($projectId, $token)
                : [];

            return [
                'id'            => $projectId,
                'name'          => $project['name'] ?? $project['title'] ?? '',
                'collaborators' => $collaborators,
            ];
        }, $projects);

        return $this->json([
            'success'  => true,
            'projects' => $projectsWithCollaborators,
        ]);
    }

    // ── POST /api/tasks/create ──────────────────────────────────────
    #[Route('/create', name: 'task_create', methods: ['POST'])]
    public function create(Request $request): JsonResponse
    {
        $authHeader = $request->headers->get('Authorization', '');
        $token = str_replace('Bearer ', '', $authHeader);
        $user = $this->authClient->validateToken($token);
        if (!$user) {
            return $this->json(['error' => 'Unauthorized'], 401);
        }

        $body = json_decode($request->getContent(), true) ?? [];
        $dto  = $this->buildDto($body);

        try {
            $task = $this->taskService->create($dto);
            return $this->json(
                ['success' => true, 'data' => $task],
                201, [],
                ['groups' => ['task:read']]
            );
        } catch (\Throwable $e) {
            return $this->json(['error' => $e->getMessage()], 500);
        }
    }

    // ── GET /api/tasks/{id} ─────────────────────────────────────────
    #[Route('/{id}', name: 'task_get', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function getOne(int $id, Request $request): JsonResponse
    {
        $authHeader = $request->headers->get('Authorization', '');
        $token = str_replace('Bearer ', '', $authHeader);
        $user = $this->authClient->validateToken($token);
        if (!$user) {
            return $this->json(['error' => 'Unauthorized'], 401);
        }

        try {
            $task = $this->taskService->getById($id);
            return $this->json(
                ['success' => true, 'data' => $task],
                200, [],
                ['groups' => ['task:read']]
            );
        } catch (\Throwable $e) {
            return $this->json(['error' => $e->getMessage()], 404);
        }
    }

    // ── PUT /api/tasks/{id} ─────────────────────────────────────────
    #[Route('/{id}', name: 'task_update', methods: ['PUT'], requirements: ['id' => '\d+'])]
    public function update(int $id, Request $request): JsonResponse
    {
        $authHeader = $request->headers->get('Authorization', '');
        $token = str_replace('Bearer ', '', $authHeader);
        $user = $this->authClient->validateToken($token);
        if (!$user) {
            return $this->json(['error' => 'Unauthorized'], 401);
        }

        $body = json_decode($request->getContent(), true) ?? [];
        $dto  = $this->buildDto($body);

        try {
            $task = $this->taskService->update($id, $dto);
            return $this->json(
                ['success' => true, 'data' => $task],
                200, [],
                ['groups' => ['task:read']]
            );
        } catch (\Throwable $e) {
            return $this->json(['error' => $e->getMessage()], 500);
        }
    }

    // ── DELETE /api/tasks/{id} ──────────────────────────────────────
    #[Route('/{id}', name: 'task_delete', methods: ['DELETE'], requirements: ['id' => '\d+'])]
    public function softDelete(int $id, Request $request): JsonResponse
    {
        $authHeader = $request->headers->get('Authorization', '');
        $token = str_replace('Bearer ', '', $authHeader);
        $user = $this->authClient->validateToken($token);
        if (!$user) {
            return $this->json(['error' => 'Unauthorized'], 401);
        }

        try {
            $this->taskService->softDelete($id);
            return $this->json(['success' => true, 'message' => 'Tâche supprimée.']);
        } catch (\Throwable $e) {
            return $this->json(['error' => $e->getMessage()], 500);
        }
    }

    // ── PATCH /api/tasks/{id}/done ──────────────────────────────────
    #[Route('/{id}/done', name: 'task_done', methods: ['PATCH'], requirements: ['id' => '\d+'])]
    public function markAsDone(int $id, Request $request): JsonResponse
    {
        $authHeader = $request->headers->get('Authorization', '');
        $token = str_replace('Bearer ', '', $authHeader);
        $user = $this->authClient->validateToken($token);
        if (!$user) {
            return $this->json(['error' => 'Unauthorized'], 401);
        }

        try {
            $task = $this->taskService->markAsDone($id);
            return $this->json(
                ['success' => true, 'data' => $task],
                200, [],
                ['groups' => ['task:read']]
            );
        } catch (\Throwable $e) {
            return $this->json(['error' => $e->getMessage()], 500);
        }
    }

    // ── Helper privé ────────────────────────────────────────────────
    private function buildDto(array $body): TaskDto
    {
        $dto              = new TaskDto();
        $dto->title       = $body['title']       ?? null;
        $dto->description = $body['description'] ?? null;
        $dto->status      = $body['status']      ?? 'todo';
        $dto->priority    = $body['priority']    ?? 'medium';
        $dto->dueDate     = $body['dueDate']     ?? null;
        $dto->assignedTo  = $body['assignedTo']  ?? null;
        $dto->projectId   = isset($body['projectId']) ? (int) $body['projectId'] : null;
        return $dto;
    }
}
