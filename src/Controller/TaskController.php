<?php

namespace App\Controller;

use App\Service\TaskService;
use App\DTOs\DTOtasks\TaskDto;
use App\Repository\EpicRepository;
use App\Repository\PartRepository;
use App\Repository\LevelRepository;
use App\Repository\UserRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;

final class TaskController extends AbstractController
{
    // ---------------- CREATE TASK ----------------
    #[Route('/api/tasks', name: 'api_tasks_create', methods: ['POST'])]
    public function create(Request $request, TaskService $taskService): JsonResponse
    {
        $data = json_decode($request->getContent(), true);

        try {
            $dto = new TaskDto();
            $dto->title = $data['title'] ?? null;
            $dto->description = $data['description'] ?? null;
            $dto->status = $data['status'] ?? 'To Do';
            $dto->dueDate = $data['dueDate'] ?? null;
            $dto->assignedTo = $data['assignedTo'] ?? null;
            $dto->assignedBy = $data['assignedBy'] ?? null;
            $dto->epic = $data['epic'] ?? null;
            $dto->part = $data['part'] ?? null;
            $dto->level = $data['level'] ?? null;

            $task = $taskService->create($dto);

            return $this->json([
                'id' => $task->getId(),
                'title' => $task->getTitle(),
                'description' => $task->getDescription(),
                'status' => $task->getStatus(),
                'assignedTo' => $task->getAssignedTo()?->getId(),
                'assignedBy' => $task->getAssignedBy()?->getId(),
                'epic' => $task->getEpic()?->getId(),
                'part' => $task->getPart()?->getId(),
                'level' => $task->getLevel()?->getId(),
                'dueDate' => $task->getDueDate()?->format('Y-m-d H:i:s') ?? null
            ], 201);

        } catch (\Exception $e) {
            return $this->json(['error' => $e->getMessage()], 500);
        }
    }

    // ---------------- UPDATE TASK ----------------
    #[Route('/api/tasks/{id}', name: 'api_tasks_update', methods: ['PUT'])]
    public function update(int $id, Request $request, TaskService $taskService): JsonResponse
    {
        $data = json_decode($request->getContent(), true);

        try {
            $dto = new TaskDto();
            $dto->title = $data['title'] ?? null;
            $dto->description = $data['description'] ?? null;
            $dto->status = $data['status'] ?? null;
            $dto->dueDate = $data['dueDate'] ?? null;
            $dto->assignedTo = $data['assignedTo'] ?? null;
            $dto->assignedBy = $data['assignedBy'] ?? null;
            $dto->epic = $data['epic'] ?? null;
            $dto->part = $data['part'] ?? null;
            $dto->level = $data['level'] ?? null;

            $task = $taskService->update($id, $dto);

            return $this->json([
                'id' => $task->getId(),
                'title' => $task->getTitle(),
                'description' => $task->getDescription(),
                'status' => $task->getStatus(),
                'dueDate' => $task->getDueDate()?->format('Y-m-d H:i:s') ?? null,
                'assignedTo' => $task->getAssignedTo()?->getId(),
                'assignedBy' => $task->getAssignedBy()?->getId(),
                'epic' => $task->getEpic()?->getId(),
                'part' => $task->getPart()?->getId(),
                'level' => $task->getLevel()?->getId()
            ], 200);

        } catch (\Exception $e) {
            return $this->json(['error' => $e->getMessage()], 404);
        }
    }

    // ---------------- LIST TASKS ----------------
    #[Route('/api/tasks', name: 'api_tasks_list', methods: ['GET'])]
    public function list(TaskService $taskService): JsonResponse
    {
        try {
            $tasks = $taskService->getAll();

            $result = [];
            foreach ($tasks as $task) {
                $result[] = [
                    'id' => $task->getId(),
                    'title' => $task->getTitle() ?? '—',
                    'status' => $task->getStatus() ?? 'To Do',
                    'color' => match($task->getStatus() ?? 'To Do') {
                        'To Do' => '#f97316',
                        'In Progress' => '#3b82f6',
                        'Done' => '#22c55e',
                        default => '#6b7280'
                    },
                    'assignedTo' => $task->getAssignedTo()?->getFullName() ?? '—',
                    'assignedBy' => $task->getAssignedBy()?->getFullName() ?? '—',
                    'epic' => $task->getEpic()?->getTitle() ?? '—',
                    'part' => $task->getPart()?->getName() ?? '—',    // ✅ corrigé
                    'level' => $task->getLevel()?->getName() ?? '—',   // ✅ corrigé
                    'dueDate' => $task->getDueDate()?->format('Y-m-d H:i:s') ?? '—',
                    'createdAt' => $task->getCreatedAt()?->format('Y-m-d H:i:s') ?? '—'
                ];
            }

            return $this->json($result, 200);

        } catch (\Exception $e) {
            return $this->json(['error' => $e->getMessage()], 500);
        }
    }

    // ---------------- FORM DATA FOR TASK ----------------
    #[Route('/api/tasks/form-data', name: 'api_tasks_form_data', methods: ['GET'])]
    public function formData(
        EpicRepository $epicRepo,
        PartRepository $partRepo,
        LevelRepository $levelRepo,
        UserRepository $userRepo
    ): JsonResponse {
        $epics = $epicRepo->findAll();
        $parts = $partRepo->findAll();
        $levels = $levelRepo->findAll();
        $users = $userRepo->findAll();

        return $this->json([
            'epics' => array_map(fn($e) => ['id' => $e->getId(), 'title' => $e->getTitle()], $epics),
            'parts' => array_map(fn($p) => ['id' => $p->getId(), 'name' => $p->getName()], $parts),
            'levels' => array_map(fn($l) => ['id' => $l->getId(), 'name' => $l->getName()], $levels),
            'users' => array_map(fn($u) => ['id' => $u->getId(), 'fullName' => $u->getFullName()], $users)
        ]);
    }
    // ---------------- GET TASK BY ID ----------------
#[Route('/api/tasks/{id}', name: 'api_tasks_show', methods: ['GET'])]
public function show(int $id, TaskService $taskService): JsonResponse
{
    try {
        $task = $taskService->getById($id); // 🔥 tu dois créer cette méthode dans TaskService

        return $this->json([
            'id' => $task->getId(),
            'title' => $task->getTitle(),
            'description' => $task->getDescription(),
            'status' => $task->getStatus(),
            'dueDate' => $task->getDueDate()?->format('Y-m-d H:i:s') ?? null,
            'assignedTo' => $task->getAssignedTo()?->getId(),
            'assignedBy' => $task->getAssignedBy()?->getId(),
            'epic' => $task->getEpic()?->getId(),
            'part' => $task->getPart()?->getId(),
            'level' => $task->getLevel()?->getId()
        ]);

    } catch (\Exception $e) {
        return $this->json(['error' => $e->getMessage()], 404);
    }
}
// ---------------- SOFT DELETE TASK ----------------
#[Route('/api/tasks/{id}/soft-delete', name: 'api_tasks_soft_delete', methods: ['PUT'])]
public function softDelete(int $id, TaskService $taskService): JsonResponse
{
    try {
        $task = $taskService->softDelete($id); // 🔥 méthode à créer dans TaskService

        return $this->json([
            'message' => 'Task soft deleted successfully',
            'id' => $task->getId()
        ]);
    } catch (\Exception $e) {
        return $this->json(['error' => $e->getMessage()], 404);
    }
}
}
