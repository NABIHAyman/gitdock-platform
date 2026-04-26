<?php

namespace App\Controller;

use App\Service\TaskService;
use App\DTOs\DTOtasks\TaskDto;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/api/tasks')]
#[IsGranted('ROLE_USER')]
final class TaskController extends AbstractController
{
    public function __construct(
        private TaskService $taskService
    ) {}

    // ================= CREATE =================
    #[Route('', methods: ['POST'])]
    public function create(Request $request): JsonResponse
    {
        try {
            $dto = $this->mapRequestToDto($request);

            $task = $this->taskService->create($dto, $this->getUser());

            return $this->json($task, 201);

        } catch (\Exception $e) {
            return $this->json(['error' => $e->getMessage()], 400);
        }
    }

    // ================= LIST =================
    #[Route('', methods: ['GET'])]
    public function list(): JsonResponse
    {
        return $this->json($this->taskService->getAll());
    }

    // ================= SHOW =================
    #[Route('/{id}', methods: ['GET'])]
    public function show(int $id): JsonResponse
    {
        try {
            return $this->json($this->taskService->getById($id));
        } catch (\Exception $e) {
            return $this->json(['error' => 'Not found'], 404);
        }
    }

    // ================= UPDATE =================
    #[Route('/{id}', methods: ['PUT'])]
    public function update(int $id, Request $request): JsonResponse
    {
        try {
            $dto = $this->mapRequestToDto($request);

            $task = $this->taskService->update($id, $dto);

            return $this->json($task);

        } catch (\Exception $e) {
            return $this->json(['error' => $e->getMessage()], 400);
        }
    }

    // ================= DELETE =================
    #[Route('/{id}', methods: ['DELETE'])]
    public function delete(int $id): JsonResponse
    {
        try {
            $this->taskService->softDelete($id);

            return $this->json(['message' => 'deleted']);

        } catch (\Exception $e) {
            return $this->json(['error' => 'not found'], 404);
        }
    }

    // ================= DASHBOARD =================
    #[Route('/dashboard', methods: ['GET'])]
    public function dashboard(): JsonResponse
    {
        return $this->json($this->taskService->getDashboardStats());
    }

    // ================= PRIVATE MAPPER =================
    private function mapRequestToDto(Request $request): TaskDto
    {
        $data = json_decode($request->getContent(), true);

        $dto = new TaskDto();

        $dto->title = $data['title'] ?? null;
        $dto->description = $data['description'] ?? null;
        $dto->status = $data['status'] ?? null;
        $dto->dueDate = $data['dueDate'] ?? null;
        $dto->assignedTo = $data['assignedTo'] ?? null;
        $dto->epic = $data['epic'] ?? null;
        $dto->part = $data['part'] ?? null;
        $dto->level = $data['level'] ?? null;

        return $dto;
    }
}